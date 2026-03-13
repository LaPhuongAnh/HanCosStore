package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosInvoiceSummaryDTO {
    private String invoiceId;
    private String label;
    private int itemCount;
    private double total;
    private boolean active;
}
