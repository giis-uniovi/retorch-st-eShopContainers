import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { OrdersService } from '../orders.service';
import { IOrderDetail } from '../../shared/models/order-detail.model';
import { ActivatedRoute } from '@angular/router';

@Component({
  standalone: false,
    selector: 'esh-orders_detail .esh-orders_detail .mb-5',
    styleUrls: ['./orders-detail.component.scss'],
    templateUrl: './orders-detail.component.html'
})
export class OrdersDetailComponent implements OnInit {
    public order: IOrderDetail = <IOrderDetail>{};

    constructor(private readonly service: OrdersService, private readonly route: ActivatedRoute, private readonly cdr: ChangeDetectorRef) { }

    ngOnInit() {
        this.route.params.subscribe(params => {
            const id = +params['id']; // (+) converts string 'id' to a number
            this.getOrder(id);
        });
    }

    getOrder(id: number) {
        this.service.getOrder(id).subscribe(order => {
            this.order = order;
            this.cdr.detectChanges();
        });
    }
}