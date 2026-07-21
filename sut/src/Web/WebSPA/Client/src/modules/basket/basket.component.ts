import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { BasketService } from './basket.service';
import { IBasket } from '../shared/models/basket.model';
import { IBasketItem } from '../shared/models/basketItem.model';
import { BasketWrapperService } from '../shared/services/basket.wrapper.service';

@Component({
  standalone: false,
    selector: 'esh-basket .esh-basket .mb-5',
    styleUrls: ['./basket.component.scss'],
    templateUrl: './basket.component.html'
})
export class BasketComponent implements OnInit {
    errorMessages: any;
    basket: IBasket;
    totalPrice: number = 0;

    constructor(
        private readonly basketSerive: BasketService,
        private readonly router: Router,
        private readonly basketWrapperService: BasketWrapperService,
        private readonly cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.basketSerive.getBasket().subscribe(basket => {
            this.basket = basket;
            this.calculateTotalPrice();
            this.cdr.detectChanges();
        });
    }

    deleteItem(id: string) {
        this.basket.items = this.basket.items.filter(item => item.id !== id);
        this.calculateTotalPrice();
        
        this.basketSerive.setBasket(this.basket).subscribe(() => {
            this.basketSerive.updateQuantity();
        });
    }

    itemQuantityChanged(item: IBasketItem, quantity: number) {
        item.quantity = Math.max(quantity, 1);
        this.calculateTotalPrice();
        this.basketSerive.setBasket(this.basket).subscribe();
    }

    update(event: any) {
        const setBasketObservable = this.basketSerive.setBasket(this.basket);
        setBasketObservable.subscribe({
            next: () => { this.errorMessages = []; },
            error: errMessage => this.errorMessages = errMessage.messages
        });
        return setBasketObservable;
    }

    checkOut(event: any) {
        this.update(event)
            .subscribe(
                () => {
                    this.errorMessages = [];
                    this.basketWrapperService.basket = this.basket;
                    this.router.navigate(['order']);
        });
    }

    private calculateTotalPrice() {
        this.totalPrice = 0;
        this.basket.items.forEach(item => {
            this.totalPrice += (item.unitPrice * item.quantity);
        });
    }
}
