import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { OrdersService }        from './orders.service';
import { IOrder }               from '../shared/models/order.model';
import { ConfigurationService } from '../shared/services/configuration.service';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SignalrService } from '../shared/services/signalr.service';

@Component({
  standalone: false,
    selector: 'esh-orders .esh-orders .mb-5',
    styleUrls: ['./orders.component.scss'],
    templateUrl: './orders.component.html'
})
export class OrdersComponent implements OnInit {
    errorReceived: boolean;

    orders: IOrder[];

    constructor(
        private readonly service: OrdersService,
        private readonly configurationService: ConfigurationService,
        private readonly signalrService: SignalrService,
        private readonly cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        if (this.configurationService.isReady) {
            this.getOrders();
        } else {
            this.configurationService.settingsLoaded$.subscribe(() => {
                this.getOrders();
            });
        }

        this.signalrService.msgReceived$
            .subscribe(() => this.getOrders());
    }

    getOrders() {
        this.errorReceived = false;
        this.service.getOrders()
            .pipe(catchError((err) => this.handleError(err)))
            .subscribe(orders => {
                this.orders = orders;
                this.cdr.detectChanges();
        });
    }

    cancelOrder(orderNumber) {
        this.errorReceived = false;
        this.service.cancelOrder(orderNumber)
            .pipe(catchError((err) => this.handleError(err)))
            .subscribe();
    }

    private handleError(error: any) {
        this.errorReceived = true;
        return throwError(() => error);
    }  
}

