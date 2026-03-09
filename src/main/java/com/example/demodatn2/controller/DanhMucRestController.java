package com.example.demodatn2.controller;

import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.service.DanhMucService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/danh-muc")
@RequiredArgsConstructor
public class DanhMucRestController {

    private final DanhMucService danhMucService;

    @GetMapping
    public List<DanhMuc> getAll() {
        return danhMucService.getActive();
    }

    @GetMapping("/{parentId}/children")
    public List<DanhMuc> getChildren(@PathVariable Integer parentId) {
        return danhMucService.getByParentId(parentId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            danhMucService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
