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
    private oldOrders: IOrder[];
    private readonly interval = null;
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
            this.configurationService.settingsLoaded$.subscribe(x => {
                this.getOrders();
            });
        }

        this.signalrService.msgReceived$
            .subscribe(x => this.getOrders());
    }

    getOrders() {
        this.errorReceived = false;
        this.service.getOrders()
            .pipe(catchError((err) => this.handleError(err)))
            .subscribe(orders => {
                this.orders = orders;
                this.oldOrders = this.orders;
                console.log('orders items retrieved: ' + orders.length);
                this.cdr.detectChanges();
        });
    }

    cancelOrder(orderNumber) {
        this.errorReceived = false;
        this.service.cancelOrder(orderNumber)
            .pipe(catchError((err) => this.handleError(err)))
            .subscribe(() => {
                console.log('order canceled: ' + orderNumber);
        });
    }

    private handleError(error: any) {
        this.errorReceived = true;
        return throwError(() => error);
    }  
}

