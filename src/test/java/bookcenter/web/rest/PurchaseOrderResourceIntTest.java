package bookcenter.web.rest;

import bookcenter.BookCenterApp;

import bookcenter.domain.PurchaseOrder;
import bookcenter.domain.Warehouse;
import bookcenter.domain.Employee;
import bookcenter.repository.PurchaseOrderRepository;
import bookcenter.service.PurchaseOrderService;
import bookcenter.service.dto.PurchaseOrderDTO;
import bookcenter.service.mapper.PurchaseOrderMapper;
import bookcenter.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static bookcenter.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PurchaseOrderResource REST controller.
 *
 * @see PurchaseOrderResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BookCenterApp.class)
public class PurchaseOrderResourceIntTest {

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_SUPPLIER = "AAAAAAAAAA";
    private static final String UPDATED_SUPPLIER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_AMOUNT = new BigDecimal(2);

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restPurchaseOrderMockMvc;

    private PurchaseOrder purchaseOrder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PurchaseOrderResource purchaseOrderResource = new PurchaseOrderResource(purchaseOrderService);
        this.restPurchaseOrderMockMvc = MockMvcBuilders.standaloneSetup(purchaseOrderResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PurchaseOrder createEntity(EntityManager em) {
        PurchaseOrder purchaseOrder = new PurchaseOrder()
            .date(DEFAULT_DATE)
            .supplier(DEFAULT_SUPPLIER)
            .totalAmount(DEFAULT_TOTAL_AMOUNT);
        // Add required entity
        Warehouse warehouse = WarehouseResourceIntTest.createEntity(em);
        em.persist(warehouse);
        em.flush();
        purchaseOrder.setWarehouse(warehouse);
        // Add required entity
        Employee buyer = EmployeeResourceIntTest.createEntity(em);
        em.persist(buyer);
        em.flush();
        purchaseOrder.setBuyer(buyer);
        return purchaseOrder;
    }

    @Before
    public void initTest() {
        purchaseOrder = createEntity(em);
    }

    @Test
    @Transactional
    public void createPurchaseOrder() throws Exception {
        int databaseSizeBeforeCreate = purchaseOrderRepository.findAll().size();

        // Create the PurchaseOrder
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDto(purchaseOrder);
        restPurchaseOrderMockMvc.perform(post("/api/purchase-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(purchaseOrderDTO)))
            .andExpect(status().isCreated());

        // Validate the PurchaseOrder in the database
        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();
        assertThat(purchaseOrderList).hasSize(databaseSizeBeforeCreate + 1);
        PurchaseOrder testPurchaseOrder = purchaseOrderList.get(purchaseOrderList.size() - 1);
        assertThat(testPurchaseOrder.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testPurchaseOrder.getSupplier()).isEqualTo(DEFAULT_SUPPLIER);
        assertThat(testPurchaseOrder.getTotalAmount()).isEqualTo(DEFAULT_TOTAL_AMOUNT);
    }

    @Test
    @Transactional
    public void createPurchaseOrderWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = purchaseOrderRepository.findAll().size();

        // Create the PurchaseOrder with an existing ID
        purchaseOrder.setId(1L);
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDto(purchaseOrder);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPurchaseOrderMockMvc.perform(post("/api/purchase-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(purchaseOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PurchaseOrder in the database
        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();
        assertThat(purchaseOrderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = purchaseOrderRepository.findAll().size();
        // set the field null
        purchaseOrder.setDate(null);

        // Create the PurchaseOrder, which fails.
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDto(purchaseOrder);

        restPurchaseOrderMockMvc.perform(post("/api/purchase-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(purchaseOrderDTO)))
            .andExpect(status().isBadRequest());

        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();
        assertThat(purchaseOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTotalAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = purchaseOrderRepository.findAll().size();
        // set the field null
        purchaseOrder.setTotalAmount(null);

        // Create the PurchaseOrder, which fails.
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDto(purchaseOrder);

        restPurchaseOrderMockMvc.perform(post("/api/purchase-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(purchaseOrderDTO)))
            .andExpect(status().isBadRequest());

        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();
        assertThat(purchaseOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPurchaseOrders() throws Exception {
        // Initialize the database
        purchaseOrderRepository.saveAndFlush(purchaseOrder);

        // Get all the purchaseOrderList
        restPurchaseOrderMockMvc.perform(get("/api/purchase-orders?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(purchaseOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].supplier").value(hasItem(DEFAULT_SUPPLIER.toString())))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(DEFAULT_TOTAL_AMOUNT.intValue())));
    }

    @Test
    @Transactional
    public void getPurchaseOrder() throws Exception {
        // Initialize the database
        purchaseOrderRepository.saveAndFlush(purchaseOrder);

        // Get the purchaseOrder
        restPurchaseOrderMockMvc.perform(get("/api/purchase-orders/{id}", purchaseOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(purchaseOrder.getId().intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.supplier").value(DEFAULT_SUPPLIER.toString()))
            .andExpect(jsonPath("$.totalAmount").value(DEFAULT_TOTAL_AMOUNT.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPurchaseOrder() throws Exception {
        // Get the purchaseOrder
        restPurchaseOrderMockMvc.perform(get("/api/purchase-orders/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePurchaseOrder() throws Exception {
        // Initialize the database
        purchaseOrderRepository.saveAndFlush(purchaseOrder);
        int databaseSizeBeforeUpdate = purchaseOrderRepository.findAll().size();

        // Update the purchaseOrder
        PurchaseOrder updatedPurchaseOrder = purchaseOrderRepository.findOne(purchaseOrder.getId());
        // Disconnect from session so that the updates on updatedPurchaseOrder are not directly saved in db
        em.detach(updatedPurchaseOrder);
        updatedPurchaseOrder
            .date(UPDATED_DATE)
            .supplier(UPDATED_SUPPLIER)
            .totalAmount(UPDATED_TOTAL_AMOUNT);
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDto(updatedPurchaseOrder);

        restPurchaseOrderMockMvc.perform(put("/api/purchase-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(purchaseOrderDTO)))
            .andExpect(status().isOk());

        // Validate the PurchaseOrder in the database
        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();
        assertThat(purchaseOrderList).hasSize(databaseSizeBeforeUpdate);
        PurchaseOrder testPurchaseOrder = purchaseOrderList.get(purchaseOrderList.size() - 1);
        assertThat(testPurchaseOrder.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testPurchaseOrder.getSupplier()).isEqualTo(UPDATED_SUPPLIER);
        assertThat(testPurchaseOrder.getTotalAmount()).isEqualTo(UPDATED_TOTAL_AMOUNT);
    }

    @Test
    @Transactional
    public void updateNonExistingPurchaseOrder() throws Exception {
        int databaseSizeBeforeUpdate = purchaseOrderRepository.findAll().size();

        // Create the PurchaseOrder
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDto(purchaseOrder);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPurchaseOrderMockMvc.perform(put("/api/purchase-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(purchaseOrderDTO)))
            .andExpect(status().isCreated());

        // Validate the PurchaseOrder in the database
        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();
        assertThat(purchaseOrderList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePurchaseOrder() throws Exception {
        // Initialize the database
        purchaseOrderRepository.saveAndFlush(purchaseOrder);
        int databaseSizeBeforeDelete = purchaseOrderRepository.findAll().size();

        // Get the purchaseOrder
        restPurchaseOrderMockMvc.perform(delete("/api/purchase-orders/{id}", purchaseOrder.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();
        assertThat(purchaseOrderList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PurchaseOrder.class);
        PurchaseOrder purchaseOrder1 = new PurchaseOrder();
        purchaseOrder1.setId(1L);
        PurchaseOrder purchaseOrder2 = new PurchaseOrder();
        purchaseOrder2.setId(purchaseOrder1.getId());
        assertThat(purchaseOrder1).isEqualTo(purchaseOrder2);
        purchaseOrder2.setId(2L);
        assertThat(purchaseOrder1).isNotEqualTo(purchaseOrder2);
        purchaseOrder1.setId(null);
        assertThat(purchaseOrder1).isNotEqualTo(purchaseOrder2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PurchaseOrderDTO.class);
        PurchaseOrderDTO purchaseOrderDTO1 = new PurchaseOrderDTO();
        purchaseOrderDTO1.setId(1L);
        PurchaseOrderDTO purchaseOrderDTO2 = new PurchaseOrderDTO();
        assertThat(purchaseOrderDTO1).isNotEqualTo(purchaseOrderDTO2);
        purchaseOrderDTO2.setId(purchaseOrderDTO1.getId());
        assertThat(purchaseOrderDTO1).isEqualTo(purchaseOrderDTO2);
        purchaseOrderDTO2.setId(2L);
        assertThat(purchaseOrderDTO1).isNotEqualTo(purchaseOrderDTO2);
        purchaseOrderDTO1.setId(null);
        assertThat(purchaseOrderDTO1).isNotEqualTo(purchaseOrderDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(purchaseOrderMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(purchaseOrderMapper.fromId(null)).isNull();
    }
}
