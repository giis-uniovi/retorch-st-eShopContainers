namespace WebMVC.Services.ModelDTOs;

public record LocationDto
{
    public double Longitude { get; init; }
    public double Latitude { get; init; }
}
