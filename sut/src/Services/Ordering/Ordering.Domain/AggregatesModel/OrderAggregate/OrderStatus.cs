using Microsoft.eShopOnContainers.Services.Ordering.Domain.SeedWork;

namespace Microsoft.eShopOnContainers.Services.Ordering.Domain.AggregatesModel.OrderAggregate;

public class OrderStatus
    : Enumeration
{
    public static readonly OrderStatus Submitted = new OrderStatus(1, nameof(Submitted).ToLowerInvariant());
    public static readonly OrderStatus AwaitingValidation = new OrderStatus(2, nameof(AwaitingValidation).ToLowerInvariant());
    public static readonly OrderStatus StockConfirmed = new OrderStatus(3, nameof(StockConfirmed).ToLowerInvariant());
    public static readonly OrderStatus Paid = new OrderStatus(4, nameof(Paid).ToLowerInvariant());
    public static readonly OrderStatus Shipped = new OrderStatus(5, nameof(Shipped).ToLowerInvariant());
    public static readonly OrderStatus Cancelled = new OrderStatus(6, nameof(Cancelled).ToLowerInvariant());

    public OrderStatus(int id, string name)
        : base(id, name)
    {
    }

    private static readonly OrderStatus[] s_list = [Submitted, AwaitingValidation, StockConfirmed, Paid, Shipped, Cancelled];

    public static IEnumerable<OrderStatus> List() => s_list;

    public static OrderStatus FromName(string name)
    {
        var state = List()
            .SingleOrDefault(s => string.Equals(s.Name, name, StringComparison.CurrentCultureIgnoreCase));

        if (state == null)
        {
            throw new OrderingDomainException($"Possible values for OrderStatus: {string.Join(",", List().Select(s => s.Name))}");
        }

        return state;
    }

    public static OrderStatus From(int id)
    {
        var state = List().SingleOrDefault(s => s.Id == id);

        if (state == null)
        {
            throw new OrderingDomainException($"Possible values for OrderStatus: {string.Join(",", List().Select(s => s.Name))}");
        }

        return state;
    }
}
