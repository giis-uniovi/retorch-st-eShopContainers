namespace Microsoft.eShopOnContainers.Services.Ordering.Domain.AggregatesModel.BuyerAggregate;

public class PaymentMethod : Entity
{
    private string _alias; // NOSONAR - EF Core backing field (PaymentMethodEntityTypeConfiguration maps this to the Alias column)
    private readonly string _cardNumber;
    private string _cardHolderName; // NOSONAR - EF Core backing field (PaymentMethodEntityTypeConfiguration maps this to the CardHolderName column)
    private readonly DateTime _expiration;

    private readonly int _cardTypeId;
    public CardType CardType { get; }

    protected PaymentMethod() { }

    public PaymentMethod(int cardTypeId, string alias, string cardNumber, string securityNumber, string cardHolderName, DateTime expiration)
    {
        _cardNumber = !string.IsNullOrWhiteSpace(cardNumber) ? cardNumber : throw new OrderingDomainException(nameof(cardNumber));
        if (string.IsNullOrWhiteSpace(securityNumber)) throw new OrderingDomainException(nameof(securityNumber));
        _cardHolderName = !string.IsNullOrWhiteSpace(cardHolderName) ? cardHolderName : throw new OrderingDomainException(nameof(cardHolderName));

        if (expiration < DateTime.UtcNow)
        {
            throw new OrderingDomainException(nameof(expiration));
        }

        _alias = alias;
        _expiration = expiration;
        _cardTypeId = cardTypeId;
    }

    public bool IsEqualTo(int cardTypeId, string cardNumber, DateTime expiration)
    {
        return _cardTypeId == cardTypeId
            && _cardNumber == cardNumber
            && _expiration == expiration;
    }
}
