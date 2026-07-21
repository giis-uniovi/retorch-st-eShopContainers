namespace Microsoft.eShopOnContainers.Services.Ordering.API.Application.Commands;

public class CancelOrderCommand : IRequest<bool>
{

    [DataMember]
    [Required, System.Text.Json.Serialization.JsonRequired]
    public int OrderNumber { get; set; }
    public CancelOrderCommand()
    {

    }
    public CancelOrderCommand(int orderNumber)
    {
        OrderNumber = orderNumber;
    }
}
