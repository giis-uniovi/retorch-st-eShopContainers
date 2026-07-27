namespace Microsoft.eShopOnContainers.BuildingBlocks.IntegrationEventLogEF;

public enum EventState
{
    NotPublished = 0,
    InProgress = 1,
    Published = 2,
    PublishedFailed = 3
}

