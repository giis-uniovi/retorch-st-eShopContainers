namespace WebMVC.Services.ModelDTOs;

public record OrderDto
{
    [Required]
    public string OrderNumber { get; init; }
}
