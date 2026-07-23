namespace Microsoft.eShopOnContainers.BuildingBlocks.EventBusRabbitMQ;

public class DefaultRabbitMQPersistentConnection // NOSONAR S3881
    : IRabbitMQPersistentConnection
{
    private readonly IConnectionFactory _connectionFactory;
    private readonly ILogger<DefaultRabbitMQPersistentConnection> _logger;
    private readonly int _retryCount;
    private IConnection _connection;
    public bool Disposed { get; private set; }

    readonly SemaphoreSlim _syncRoot = new(1, 1);

    public DefaultRabbitMQPersistentConnection(IConnectionFactory connectionFactory, ILogger<DefaultRabbitMQPersistentConnection> logger, int retryCount = 5)
    {
        _connectionFactory = connectionFactory ?? throw new ArgumentNullException(nameof(connectionFactory));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        _retryCount = retryCount;
    }

    public bool IsConnected => _connection is { IsOpen: true } && !Disposed;

    public async Task<IChannel> CreateChannelAsync()
    {
        if (!IsConnected)
        {
            throw new InvalidOperationException("No RabbitMQ connections are available to perform this action");
        }

        return await _connection.CreateChannelAsync();
    }

    public void Dispose()
    {
        if (Disposed) return;

        Disposed = true;
        GC.SuppressFinalize(this);

        try
        {
            _connection.ConnectionShutdownAsync -= OnConnectionShutdownAsync;
            _connection.CallbackExceptionAsync -= OnCallbackExceptionAsync;
            _connection.ConnectionBlockedAsync -= OnConnectionBlockedAsync;
            _connection.Dispose();
        }
        catch (IOException ex)
        {
            if (_logger.IsEnabled(LogLevel.Critical))
                _logger.LogCritical(ex, "RabbitMQ connection could not be disposed properly.");
        }
    }

    public async Task<bool> TryConnectAsync()
    {
        _logger.LogInformation("RabbitMQ Client is trying to connect");

        await _syncRoot.WaitAsync();
        try
        {
            var policy = RetryPolicy.Handle<SocketException>()
                .Or<BrokerUnreachableException>()
                .WaitAndRetryAsync(_retryCount, retryAttempt => TimeSpan.FromSeconds(Math.Pow(2, retryAttempt)), (ex, time) =>
                {
                    if (_logger.IsEnabled(LogLevel.Warning))
                        _logger.LogWarning(ex, "RabbitMQ Client could not connect after {TimeOut:n1}s", time.TotalSeconds);
                }
            );

            await policy.ExecuteAsync(async () =>
            {
                _connection = await _connectionFactory.CreateConnectionAsync();
            });

            if (IsConnected)
            {
                _connection.ConnectionShutdownAsync += OnConnectionShutdownAsync;
                _connection.CallbackExceptionAsync += OnCallbackExceptionAsync;
                _connection.ConnectionBlockedAsync += OnConnectionBlockedAsync;

                if (_logger.IsEnabled(LogLevel.Information))
                    _logger.LogInformation("RabbitMQ Client acquired a persistent connection to '{HostName}' and is subscribed to failure events", _connection.Endpoint.HostName);

                return true;
            }
            else
            {
                _logger.LogCritical("Fatal error: RabbitMQ connections could not be created and opened");

                return false;
            }
        }
        finally
        {
            _syncRoot.Release();
        }
    }

    private async Task OnConnectionBlockedAsync(object sender, ConnectionBlockedEventArgs e)
    {
        if (Disposed) return;

        _logger.LogWarning("A RabbitMQ connection is shutdown. Trying to re-connect...");

        await TryConnectAsync();
    }

    async Task OnCallbackExceptionAsync(object sender, CallbackExceptionEventArgs e)
    {
        if (Disposed) return;

        _logger.LogWarning("A RabbitMQ connection throw exception. Trying to re-connect...");

        await TryConnectAsync();
    }

    async Task OnConnectionShutdownAsync(object sender, ShutdownEventArgs reason)
    {
        if (Disposed) return;

        _logger.LogWarning("A RabbitMQ connection is on shutdown. Trying to re-connect...");

        await TryConnectAsync();
    }
}
