import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: false,
        name: 'appfUppercase'
})
export class UppercasePipe implements PipeTransform {
    transform(value: string) {
        return value.toUpperCase();
    }
}
