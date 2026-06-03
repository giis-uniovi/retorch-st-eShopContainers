import { Component, Input } from '@angular/core';

@Component({
  standalone: false,
    selector: 'esh-header',
    templateUrl: './header.html',
    styleUrls: ['./header.scss']
})
export class Header {
    @Input()
    url: string;
}
