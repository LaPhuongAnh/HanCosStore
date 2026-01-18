package com.example.demodatn2.service;

import com.example.demodatn2.dto.HomeProductVM;
import com.example.demodatn2.dto.ProductDetailVM;

import java.util.List;

public interface HomeService {
    List<HomeProductVM> getHomeProducts(Integer danhMucId, String keyword);
    ProductDetailVM getProductDetail(Integer id);
}
