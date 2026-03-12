package com.example.demodatn2.service;

import com.example.demodatn2.dto.HomeProductVM;
import com.example.demodatn2.dto.ProductDetailVM;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Contract cho dữ liệu trang chủ và trang chi tiết sản phẩm.
public interface HomeService {
    List<HomeProductVM> getHomeProducts(Integer danhMucId, String keyword);
    Page<HomeProductVM> getHomeProductsPage(Integer danhMucId, String keyword, Pageable pageable);
    ProductDetailVM getProductDetail(Integer id);
}
