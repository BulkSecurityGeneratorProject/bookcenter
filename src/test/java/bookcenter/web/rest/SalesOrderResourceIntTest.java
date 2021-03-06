package bookcenter.web.rest;

import bookcenter.BookCenterApp;

import bookcenter.domain.SalesOrder;
import bookcenter.domain.Warehouse;
import bookcenter.domain.Employee;
import bookcenter.repository.SalesOrderRepository;
import bookcenter.service.SalesOrderService;
import bookcenter.service.dto.SalesOrderDTO;
import bookcenter.service.mapper.SalesOrderMapper;
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
 * Test class for the SalesOrderResource REST controller.
 *
 * @see SalesOrderResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BookCenterApp.class)
public class SalesOrderResourceIntTest {

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_CUSTOMER = "AAAAAAAAAA";
    private static final String UPDATED_CUSTOMER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_AMOUNT = new BigDecimal(2);

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSalesOrderMockMvc;

    private SalesOrder salesOrder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SalesOrderResource salesOrderResource = new SalesOrderResource(salesOrderService);
        this.restSalesOrderMockMvc = MockMvcBuilders.standaloneSetup(salesOrderResource)
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
    public static SalesOrder createEntity(EntityManager em) {
        SalesOrder salesOrder = new SalesOrder()
            .date(DEFAULT_DATE)
            .customer(DEFAULT_CUSTOMER)
            .totalAmount(DEFAULT_TOTAL_AMOUNT);
        // Add required entity
        Warehouse warehouse = WarehouseResourceIntTest.createEntity(em);
        em.persist(warehouse);
        em.flush();
        salesOrder.setWarehouse(warehouse);
        // Add required entity
        Employee seller = EmployeeResourceIntTest.createEntity(em);
        em.persist(seller);
        em.flush();
        salesOrder.setSeller(seller);
        return salesOrder;
    }

    @Before
    public void initTest() {
        salesOrder = createEntity(em);
    }

    @Test
    @Transactional
    public void createSalesOrder() throws Exception {
        int databaseSizeBeforeCreate = salesOrderRepository.findAll().size();

        // Create the SalesOrder
        SalesOrderDTO salesOrderDTO = salesOrderMapper.toDto(salesOrder);
        restSalesOrderMockMvc.perform(post("/api/sales-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(salesOrderDTO)))
            .andExpect(status().isCreated());

        // Validate the SalesOrder in the database
        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        assertThat(salesOrderList).hasSize(databaseSizeBeforeCreate + 1);
        SalesOrder testSalesOrder = salesOrderList.get(salesOrderList.size() - 1);
        assertThat(testSalesOrder.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testSalesOrder.getCustomer()).isEqualTo(DEFAULT_CUSTOMER);
        assertThat(testSalesOrder.getTotalAmount()).isEqualTo(DEFAULT_TOTAL_AMOUNT);
    }

    @Test
    @Transactional
    public void createSalesOrderWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = salesOrderRepository.findAll().size();

        // Create the SalesOrder with an existing ID
        salesOrder.setId(1L);
        SalesOrderDTO salesOrderDTO = salesOrderMapper.toDto(salesOrder);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSalesOrderMockMvc.perform(post("/api/sales-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(salesOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the SalesOrder in the database
        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        assertThat(salesOrderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = salesOrderRepository.findAll().size();
        // set the field null
        salesOrder.setDate(null);

        // Create the SalesOrder, which fails.
        SalesOrderDTO salesOrderDTO = salesOrderMapper.toDto(salesOrder);

        restSalesOrderMockMvc.perform(post("/api/sales-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(salesOrderDTO)))
            .andExpect(status().isBadRequest());

        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        assertThat(salesOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTotalAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = salesOrderRepository.findAll().size();
        // set the field null
        salesOrder.setTotalAmount(null);

        // Create the SalesOrder, which fails.
        SalesOrderDTO salesOrderDTO = salesOrderMapper.toDto(salesOrder);

        restSalesOrderMockMvc.perform(post("/api/sales-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(salesOrderDTO)))
            .andExpect(status().isBadRequest());

        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        assertThat(salesOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSalesOrders() throws Exception {
        // Initialize the database
        salesOrderRepository.saveAndFlush(salesOrder);

        // Get all the salesOrderList
        restSalesOrderMockMvc.perform(get("/api/sales-orders?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(salesOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].customer").value(hasItem(DEFAULT_CUSTOMER.toString())))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(DEFAULT_TOTAL_AMOUNT.intValue())));
    }

    @Test
    @Transactional
    public void getSalesOrder() throws Exception {
        // Initialize the database
        salesOrderRepository.saveAndFlush(salesOrder);

        // Get the salesOrder
        restSalesOrderMockMvc.perform(get("/api/sales-orders/{id}", salesOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(salesOrder.getId().intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.customer").value(DEFAULT_CUSTOMER.toString()))
            .andExpect(jsonPath("$.totalAmount").value(DEFAULT_TOTAL_AMOUNT.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingSalesOrder() throws Exception {
        // Get the salesOrder
        restSalesOrderMockMvc.perform(get("/api/sales-orders/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSalesOrder() throws Exception {
        // Initialize the database
        salesOrderRepository.saveAndFlush(salesOrder);
        int databaseSizeBeforeUpdate = salesOrderRepository.findAll().size();

        // Update the salesOrder
        SalesOrder updatedSalesOrder = salesOrderRepository.findOne(salesOrder.getId());
        // Disconnect from session so that the updates on updatedSalesOrder are not directly saved in db
        em.detach(updatedSalesOrder);
        updatedSalesOrder
            .date(UPDATED_DATE)
            .customer(UPDATED_CUSTOMER)
            .totalAmount(UPDATED_TOTAL_AMOUNT);
        SalesOrderDTO salesOrderDTO = salesOrderMapper.toDto(updatedSalesOrder);

        restSalesOrderMockMvc.perform(put("/api/sales-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(salesOrderDTO)))
            .andExpect(status().isOk());

        // Validate the SalesOrder in the database
        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        assertThat(salesOrderList).hasSize(databaseSizeBeforeUpdate);
        SalesOrder testSalesOrder = salesOrderList.get(salesOrderList.size() - 1);
        assertThat(testSalesOrder.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testSalesOrder.getCustomer()).isEqualTo(UPDATED_CUSTOMER);
        assertThat(testSalesOrder.getTotalAmount()).isEqualTo(UPDATED_TOTAL_AMOUNT);
    }

    @Test
    @Transactional
    public void updateNonExistingSalesOrder() throws Exception {
        int databaseSizeBeforeUpdate = salesOrderRepository.findAll().size();

        // Create the SalesOrder
        SalesOrderDTO salesOrderDTO = salesOrderMapper.toDto(salesOrder);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSalesOrderMockMvc.perform(put("/api/sales-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(salesOrderDTO)))
            .andExpect(status().isCreated());

        // Validate the SalesOrder in the database
        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        assertThat(salesOrderList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSalesOrder() throws Exception {
        // Initialize the database
        salesOrderRepository.saveAndFlush(salesOrder);
        int databaseSizeBeforeDelete = salesOrderRepository.findAll().size();

        // Get the salesOrder
        restSalesOrderMockMvc.perform(delete("/api/sales-orders/{id}", salesOrder.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        assertThat(salesOrderList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalesOrder.class);
        SalesOrder salesOrder1 = new SalesOrder();
        salesOrder1.setId(1L);
        SalesOrder salesOrder2 = new SalesOrder();
        salesOrder2.setId(salesOrder1.getId());
        assertThat(salesOrder1).isEqualTo(salesOrder2);
        salesOrder2.setId(2L);
        assertThat(salesOrder1).isNotEqualTo(salesOrder2);
        salesOrder1.setId(null);
        assertThat(salesOrder1).isNotEqualTo(salesOrder2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalesOrderDTO.class);
        SalesOrderDTO salesOrderDTO1 = new SalesOrderDTO();
        salesOrderDTO1.setId(1L);
        SalesOrderDTO salesOrderDTO2 = new SalesOrderDTO();
        assertThat(salesOrderDTO1).isNotEqualTo(salesOrderDTO2);
        salesOrderDTO2.setId(salesOrderDTO1.getId());
        assertThat(salesOrderDTO1).isEqualTo(salesOrderDTO2);
        salesOrderDTO2.setId(2L);
        assertThat(salesOrderDTO1).isNotEqualTo(salesOrderDTO2);
        salesOrderDTO1.setId(null);
        assertThat(salesOrderDTO1).isNotEqualTo(salesOrderDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(salesOrderMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(salesOrderMapper.fromId(null)).isNull();
    }
}
