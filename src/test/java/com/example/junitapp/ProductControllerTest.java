package com.example.junitapp;

import com.example.junitapp.controller.ProductController;
import com.example.junitapp.entity.Product;
import com.example.junitapp.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProductService productService;

    private Product product1;
    private Product product2;

    @BeforeEach
    public void setup() {
        product1 = new Product(1L, "Product 1", 100.0, "Description 1");
        product2 = new Product(2L, "Product 2", 200.0, "Description 2");
    }

    @Test
    public void testGetAllProducts() throws Exception {
        Mockito.when(productService.getAllProducts()).thenReturn(Arrays.asList(product1, product2));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].price").value(200.0));
    }

    @Test
    public void testGetProductById_found() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.of(product1));

        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    public void testGetProductById_notFound() throws Exception {
        Mockito.when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateProduct() throws Exception {
        Product toSave = new Product(null, "New Product", 150.0, "New Desc");
        Product saved = new Product(3L, "New Product", 150.0, "New Desc");

        Mockito.when(productService.saveProduct(any(Product.class))).thenReturn(saved);

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(toSave)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    public void testUpdateProduct_found() throws Exception {
        Product updated = new Product(1L, "Updated", 999.0, "Updated Desc");

        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.of(product1));
        Mockito.when(productService.saveProduct(any(Product.class))).thenReturn(updated);

        mvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.price").value(999.0));
    }

    @Test
    public void testUpdateProduct_notFound() throws Exception {
        Mockito.when(productService.getProductById(99L)).thenReturn(Optional.empty());

        Product dummy = new Product(99L, "X", 0.0, "x");

        mvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dummy)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteProduct_found() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.of(product1));

        mvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteProduct_notFound() throws Exception {
        Mockito.when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }
}
