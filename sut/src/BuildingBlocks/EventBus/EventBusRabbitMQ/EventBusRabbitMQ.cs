namespace Microsoft.eShopOnContainers.BuildingBlocks.EventBusRabbitMQ;
using Microsoft.Extensions.DependencyInjection;

public class EventBusRabbitMQ : IEventBus, IDisposable // NOSONAR S3881
{
    const string BROKER_NAME = "eshop_event_bus";

    private static readonly JsonSerializerOptions s_indentedOptions = new() { WriteIndented = true };
    private static readonly JsonSerializerOptions s_caseInsensitiveOptions = new() { PropertyNameCaseInsensitive = true };

    private readonly IRabbitMQPersistentConnection _persistentConnection;
    private readonly ILogger<EventBusRabbitMQ> _logger;
    private readonly IEventBusSubscriptionsManager _subsManager;
    private readonly IServiceProvider _serviceProvider;
    private readonly int _retryCount;

    private IChannel _consumerChannel;
    private string _queueName;

    public EventBusRabbitMQ(IRabbitMQPersistentConnection persistentConnection, ILogger<EventBusRabbitMQ> logger,
        IServiceProvider serviceProvider, IEventBusSubscriptionsManager subsManager, string queueName = null, int retryCount = 5)
    {
        _persistentConnection = persistentConnection ?? throw new ArgumentNullException(nameof(persistentConnection));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        _subsManager = subsManager ?? new InMemoryEventBusSubscriptionsManager();
        _queueName = queueName;
        _consumerChannel = CreateConsumerChannelAsync().GetAwaiter().GetResult();
        _serviceProvider = serviceProvider;
        _retryCount = retryCount;
        _subsManager.OnEventRemoved += SubsManager_OnEventRemoved;
    }

    private void SubsManager_OnEventRemoved(object sender, string eventName)
    {
        if (!_persistentConnection.IsConnected)
        {
            _persistentConnection.TryConnectAsync().GetAwaiter().GetResult();
        }

        using var channel = _persistentConnection.CreateChannelAsync().GetAwaiter().GetResult();
        channel.QueueUnbindAsync(queue: _queueName,
            exchange: BROKER_NAME,
            routingKey: eventName).GetAwaiter().GetResult();

        if (_subsManager.IsEmpty)
        {
            _queueName = string.Empty;
            _consumerChannel.CloseAsync().GetAwaiter().GetResult();
        }
    }

    public void Publish(IntegrationEvent @event)
    {
        if (!_persistentConnection.IsConnected)
        {
            _persistentConnection.TryConnectAsync().GetAwaiter().GetResult();
        }

        var policy = RetryPolicy.Handle<BrokerUnreachableException>()
            .Or<SocketException>()
            .WaitAndRetry(_retryCount, retryAttempt => TimeSpan.FromSeconds(Math.Pow(2, retryAttempt)), (ex, time) =>
            {
                if (_logger.IsEnabled(LogLevel.Warning))
                    _logger.LogWarning(ex, "Could not publish event: {EventId} after {Timeout:n1}s", @event.Id, time.TotalSeconds);
            });

        var eventName = @event.GetType().Name;

        if (_logger.IsEnabled(LogLevel.Trace))
            _logger.LogTrace("Creating RabbitMQ channel to publish event: {EventId} ({EventName})", @event.Id, eventName);

        using var channel = _persistentConnection.CreateChannelAsync().GetAwaiter().GetResult();
        if (_logger.IsEnabled(LogLevel.Trace))
            _logger.LogTrace("Declaring RabbitMQ exchange to publish event: {EventId}", @event.Id);

        channel.ExchangeDeclareAsync(exchange: BROKER_NAME, type: "direct").GetAwaiter().GetResult();

        var body = JsonSerializer.SerializeToUtf8Bytes(@event, @event.GetType(), s_indentedOptions);

        policy.Execute(() =>
        {
            var properties = new BasicProperties
            {
                DeliveryMode = DeliveryModes.Persistent
            };

            if (_logger.IsEnabled(LogLevel.Trace))
                _logger.LogTrace("Publishing event to RabbitMQ: {EventId}", @event.Id);

            channel.BasicPublishAsync(
                exchange: BROKER_NAME,
                routingKey: eventName,
                mandatory: true,
                basicProperties: properties,
                body: body).AsTask().GetAwaiter().GetResult();
        });
    }

    public void SubscribeDynamic<TH>(string eventName)
        where TH : IDynamicIntegrationEventHandler
    {
        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("Subscribing to dynamic event {EventName} with {EventHandler}", eventName, typeof(TH).GetGenericTypeName());

        DoInternalSubscription(eventName);
        _subsManager.AddDynamicSubscription<TH>(eventName);
        StartBasicConsume();
    }

    public void Subscribe<T, TH>()
        where T : IntegrationEvent
        where TH : IIntegrationEventHandler<T>
    {
        var eventName = _subsManager.GetEventKey<T>();
        DoInternalSubscription(eventName);

        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("Subscribing to event {EventName} with {EventHandler}", eventName, typeof(TH).GetGenericTypeName());

        _subsManager.AddSubscription<T, TH>();
        StartBasicConsume();
    }

    private void DoInternalSubscription(string eventName)
    {
        var containsKey = _subsManager.HasSubscriptionsForEvent(eventName);
        if (!containsKey)
        {
            if (!_persistentConnection.IsConnected)
            {
                _persistentConnection.TryConnectAsync().GetAwaiter().GetResult();
            }

            _consumerChannel.QueueBindAsync(queue: _queueName,
                                exchange: BROKER_NAME,
                                routingKey: eventName).GetAwaiter().GetResult();
        }
    }

    public void Unsubscribe<T, TH>()
        where T : IntegrationEvent
        where TH : IIntegrationEventHandler<T>
    {
        var eventName = _subsManager.GetEventKey<T>();

        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("Unsubscribing from event {EventName}", eventName);

        _subsManager.RemoveSubscription<T, TH>();
    }

    public void UnsubscribeDynamic<TH>(string eventName)
        where TH : IDynamicIntegrationEventHandler
    {
        _subsManager.RemoveDynamicSubscription<TH>(eventName);
    }

    public void Dispose()
    {
        GC.SuppressFinalize(this);

        if (_consumerChannel != null)
        {
            _consumerChannel.Dispose();
        }

        _subsManager.Clear();
    }

    private void StartBasicConsume()
    {
        _logger.LogTrace("Starting RabbitMQ basic consume");

        if (_consumerChannel != null)
        {
            var consumer = new AsyncEventingBasicConsumer(_consumerChannel);

            consumer.ReceivedAsync += Consumer_Received;

            _consumerChannel.BasicConsumeAsync(
                queue: _queueName,
                autoAck: false,
                consumer: consumer).GetAwaiter().GetResult();
        }
        else
        {
            _logger.LogError("StartBasicConsume can't call on _consumerChannel == null");
        }
    }

    private async Task Consumer_Received(object sender, BasicDeliverEventArgs eventArgs)
    {
        var eventName = eventArgs.RoutingKey;
        var message = Encoding.UTF8.GetString(eventArgs.Body.Span);

        try
        {
            if (message.Contains("throw-fake-exception", StringComparison.OrdinalIgnoreCase))
            {
                throw new InvalidOperationException($"Fake exception requested: \"{message}\"");
            }

            await ProcessEvent(eventName, message);
        }
        catch (Exception ex)
        {
            if (_logger.IsEnabled(LogLevel.Warning))
                _logger.LogWarning(ex, "Error Processing message \"{Message}\"", message);
        }

        // Even on exception we take the message off the queue.
        // in a REAL WORLD app this should be handled with a Dead Letter Exchange (DLX).
        // For more information see: https://www.rabbitmq.com/dlx.html
        await _consumerChannel.BasicAckAsync(eventArgs.DeliveryTag, multiple: false);
    }

    private async Task<IChannel> CreateConsumerChannelAsync()
    {
        if (!_persistentConnection.IsConnected)
        {
            await _persistentConnection.TryConnectAsync();
        }

        _logger.LogTrace("Creating RabbitMQ consumer channel");

        var channel = await _persistentConnection.CreateChannelAsync();

        await channel.ExchangeDeclareAsync(exchange: BROKER_NAME,
                                type: "direct");

        await channel.QueueDeclareAsync(queue: _queueName,
                                durable: true,
                                exclusive: false,
                                autoDelete: false,
                                arguments: null);

        channel.CallbackExceptionAsync += async (sender, ea) =>
        {
            _logger.LogWarning(ea.Exception, "Recreating RabbitMQ consumer channel");

            _consumerChannel.Dispose();
            _consumerChannel = await CreateConsumerChannelAsync();
            StartBasicConsume();
        };

        return channel;
    }

    private async Task ProcessEvent(string eventName, string message) // NOSONAR S3776
    {
        if (_logger.IsEnabled(LogLevel.Trace))
            _logger.LogTrace("Processing RabbitMQ event: {EventName}", eventName);

        if (_subsManager.HasSubscriptionsForEvent(eventName))
        {
            await using var scope = _serviceProvider.CreateAsyncScope();
            var subscriptions = _subsManager.GetHandlersForEvent(eventName);
            foreach (var subscription in subscriptions)
            {
                if (subscription.IsDynamic)
                {
                    if (scope.ServiceProvider.GetService(subscription.HandlerType) is not IDynamicIntegrationEventHandler handler) continue;
                    using dynamic eventData = JsonDocument.Parse(message);
                    await Task.Yield();
                    await handler.Handle(eventData);
                }
                else
                {
                    var handler = scope.ServiceProvider.GetService(subscription.HandlerType);
                    if (handler == null) continue;
                    var eventType = _subsManager.GetEventTypeByName(eventName);
                    var integrationEvent = JsonSerializer.Deserialize(message, eventType, s_caseInsensitiveOptions);
                    var concreteType = typeof(IIntegrationEventHandler<>).MakeGenericType(eventType);

                    await Task.Yield();
                    await (Task)concreteType.GetMethod("Handle").Invoke(handler, new object[] { integrationEvent });
                }
            }
        }
        else
        {
            if (_logger.IsEnabled(LogLevel.Warning))
                _logger.LogWarning("No subscription for RabbitMQ event: {EventName}", eventName);
        }
    }
}
