namespace Microsoft.eShopOnContainers.BuildingBlocks.EventBusRabbitMQ;

public interface IRabbitMQPersistentConnection
    : IDisposable
{
    bool IsConnected { get; }

    Task<bool> TryConnectAsync();

    Task<IChannel> CreateChannelAsync();
}
