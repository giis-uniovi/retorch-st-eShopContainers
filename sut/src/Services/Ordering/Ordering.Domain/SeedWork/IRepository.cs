namespace Microsoft.eShopOnContainers.Services.Ordering.Domain.Seedwork;

public interface IRepository<T> where T : IAggregateRoot // NOSONAR S2326 - T in constraint enforces aggregate root pattern
{
    IUnitOfWork UnitOfWork { get; }
}
