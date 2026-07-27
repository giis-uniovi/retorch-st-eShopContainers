namespace Webhooks.API.Controllers;

public class WebhookSubscriptionRequest : IValidatableObject
{
    private static readonly string[] s_grantUrlMembers = [nameof(GrantUrl)];
    private static readonly string[] s_urlMembers = [nameof(Url)];
    private static readonly string[] s_eventMembers = [nameof(Event)];

    public string Url { get; set; }
    public string Token { get; set; }
    public string Event { get; set; }
    public string GrantUrl { get; set; }

    public IEnumerable<ValidationResult> Validate(ValidationContext validationContext)
    {
        if (!Uri.IsWellFormedUriString(GrantUrl, UriKind.Absolute))
        {
            yield return new ValidationResult("GrantUrl is not valid", s_grantUrlMembers);
        }

        if (!Uri.IsWellFormedUriString(Url, UriKind.Absolute))
        {
            yield return new ValidationResult("Url is not valid", s_urlMembers);
        }

        var isOk = Enum.TryParse<WebhookType>(Event, ignoreCase: true, result: out _);
        if (!isOk)
        {
            yield return new ValidationResult($"{Event} is invalid event name", s_eventMembers);
        }
    }

}
