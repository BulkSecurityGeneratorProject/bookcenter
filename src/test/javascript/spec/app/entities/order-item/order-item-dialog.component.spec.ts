/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, async, inject, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs/Observable';
import { JhiEventManager } from 'ng-jhipster';

import { BookCenterTestModule } from '../../../test.module';
import { OrderItemDialogComponent } from '../../../../../../main/webapp/app/entities/order-item/order-item-dialog.component';
import { OrderItemService } from '../../../../../../main/webapp/app/entities/order-item/order-item.service';
import { OrderItem } from '../../../../../../main/webapp/app/entities/order-item/order-item.model';
import { PurchaseOrderService } from '../../../../../../main/webapp/app/entities/purchase-order';
import { SalesOrderService } from '../../../../../../main/webapp/app/entities/sales-order';
import { BookService } from '../../../../../../main/webapp/app/entities/book';

describe('Component Tests', () => {

    describe('OrderItem Management Dialog Component', () => {
        let comp: OrderItemDialogComponent;
        let fixture: ComponentFixture<OrderItemDialogComponent>;
        let service: OrderItemService;
        let mockEventManager: any;
        let mockActiveModal: any;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [BookCenterTestModule],
                declarations: [OrderItemDialogComponent],
                providers: [
                    PurchaseOrderService,
                    SalesOrderService,
                    BookService,
                    OrderItemService
                ]
            })
            .overrideTemplate(OrderItemDialogComponent, '')
            .compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(OrderItemDialogComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(OrderItemService);
            mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
            mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
        });

        describe('save', () => {
            it('Should call update service on save for existing entity',
                inject([],
                    fakeAsync(() => {
                        // GIVEN
                        const entity = new OrderItem(123);
                        spyOn(service, 'update').and.returnValue(Observable.of(new HttpResponse({body: entity})));
                        comp.orderItem = entity;
                        // WHEN
                        comp.save();
                        tick(); // simulate async

                        // THEN
                        expect(service.update).toHaveBeenCalledWith(entity);
                        expect(comp.isSaving).toEqual(false);
                        expect(mockEventManager.broadcastSpy).toHaveBeenCalledWith({ name: 'orderItemListModification', content: 'OK'});
                        expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
                    })
                )
            );

            it('Should call create service on save for new entity',
                inject([],
                    fakeAsync(() => {
                        // GIVEN
                        const entity = new OrderItem();
                        spyOn(service, 'create').and.returnValue(Observable.of(new HttpResponse({body: entity})));
                        comp.orderItem = entity;
                        // WHEN
                        comp.save();
                        tick(); // simulate async

                        // THEN
                        expect(service.create).toHaveBeenCalledWith(entity);
                        expect(comp.isSaving).toEqual(false);
                        expect(mockEventManager.broadcastSpy).toHaveBeenCalledWith({ name: 'orderItemListModification', content: 'OK'});
                        expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
                    })
                )
            );
        });
    });

});
