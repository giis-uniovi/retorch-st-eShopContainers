import { Component, OnChanges, Output, Input, EventEmitter } from '@angular/core';

import { IPager } from '../../models/pager.model';

@Component({
  standalone: false,
    selector: 'esh-pager',
    templateUrl: './pager.html',
    styleUrls: ['./pager.scss']
})
export class Pager implements OnChanges {

    @Output()
    changed: EventEmitter<number> = new EventEmitter<number>();

    @Input()
    model: IPager;

    buttonStates: any = {
        nextDisabled: true,
        previousDisabled: true,
    };

    ngOnChanges() {
        if (this.model) {
            this.model.items = Math.min(this.model.itemsPage, this.model.totalItems);

            this.buttonStates.previousDisabled = (this.model.actualPage == 0);
            this.buttonStates.nextDisabled = (this.model.actualPage + 1 >= this.model.totalPages);
        }
    }

    onNextClicked(event: any) {
        event.preventDefault();
        console.log('Pager next clicked');
        this.changed.emit(this.model.actualPage + 1);
    }

    onPreviousCliked(event: any) {
        event.preventDefault();
        console.log('Pager previous clicked');
        this.changed.emit(this.model.actualPage - 1);
    }
}
