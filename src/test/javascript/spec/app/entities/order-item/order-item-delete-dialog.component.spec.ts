/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, async, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs/Observable';
import { JhiEventManager } from 'ng-jhipster';

import { BookCenterTestModule } from '../../../test.module';
import { OrderItemDeleteDialogComponent } from '../../../../../../main/webapp/app/entities/order-item/order-item-delete-dialog.component';
import { OrderItemService } from '../../../../../../main/webapp/app/entities/order-item/order-item.service';

describe('Component Tests', () => {

    describe('OrderItem Management Delete Component', () => {
        let comp: OrderItemDeleteDialogComponent;
        let fixture: ComponentFixture<OrderItemDeleteDialogComponent>;
        let service: OrderItemService;
        let mockEventManager: any;
        let mockActiveModal: any;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [BookCenterTestModule],
                declarations: [OrderItemDeleteDialogComponent],
                providers: [
                    OrderItemService
                ]
            })
            .overrideTemplate(OrderItemDeleteDialogComponent, '')
            .compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(OrderItemDeleteDialogComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(OrderItemService);
            mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
            mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
        });

        describe('confirmDelete', () => {
            it('Should call delete service on confirmDelete',
                inject([],
                    fakeAsync(() => {
                        // GIVEN
                        spyOn(service, 'delete').and.returnValue(Observable.of({}));

                        // WHEN
                        comp.confirmDelete(123);
                        tick();

                        // THEN
                        expect(service.delete).toHaveBeenCalledWith(123);
                        expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
                        expect(mockEventManager.broadcastSpy).toHaveBeenCalled();
                    })
                )
            );
        });
    });

});
