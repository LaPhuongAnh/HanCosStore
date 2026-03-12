-- Migration: Standardize DON_HANG.TrangThai to the new 6-status model
-- Target statuses:
--   CHO_XAC_NHAN, DA_XAC_NHAN, DANG_GIAO, HOAN_THANH, DA_HUY, TRA_HANG
--
-- Notes:
-- 1) Script is designed for SQL Server.
-- 2) Run on production only after backup.
-- 3) This script keeps return-request workflow in YEU_CAU_DOI_TRA unchanged
--    (PENDING/APPROVED/REJECTED), only migrates DON_HANG.TrangThai.

BEGIN TRY
    BEGIN TRANSACTION;

    PRINT N'=== Before migration: DON_HANG.TrangThai distribution ===';
    SELECT TrangThai, COUNT(*) AS SoLuong
    FROM DON_HANG
    GROUP BY TrangThai
    ORDER BY SoLuong DESC;

    -- Pending states -> CHO_XAC_NHAN
    UPDATE DON_HANG
    SET TrangThai = N'CHO_XAC_NHAN'
    WHERE TrangThai IN (N'PENDING', N'PENDING_PAYMENT');

    -- Confirmed states -> DA_XAC_NHAN
    UPDATE DON_HANG
    SET TrangThai = N'DA_XAC_NHAN'
    WHERE TrangThai IN (N'CONFIRMED', N'PROCESSING');

    -- Shipping states -> DANG_GIAO
    UPDATE DON_HANG
    SET TrangThai = N'DANG_GIAO'
    WHERE TrangThai IN (N'SHIPPING', N'SHIPPED');

    -- Completed states -> HOAN_THANH
    UPDATE DON_HANG
    SET TrangThai = N'HOAN_THANH'
    WHERE TrangThai IN (N'DELIVERED', N'COMPLETED');

    -- Cancelled -> DA_HUY
    UPDATE DON_HANG
    SET TrangThai = N'DA_HUY'
    WHERE TrangThai = N'CANCELLED';

    -- Return-related -> TRA_HANG
    UPDATE DON_HANG
    SET TrangThai = N'TRA_HANG'
    WHERE TrangThai IN (N'RETURN_REQUESTED', N'RETURNED');

    PRINT N'=== After migration: DON_HANG.TrangThai distribution ===';
    SELECT TrangThai, COUNT(*) AS SoLuong
    FROM DON_HANG
    GROUP BY TrangThai
    ORDER BY SoLuong DESC;

    -- Validation: show any unexpected values remaining
    PRINT N'=== Validation: unexpected statuses (should be 0 rows) ===';
    SELECT Id, MaDonHang, TrangThai
    FROM DON_HANG
    WHERE TrangThai NOT IN (
        N'CHO_XAC_NHAN',
        N'DA_XAC_NHAN',
        N'DANG_GIAO',
        N'HOAN_THANH',
        N'DA_HUY',
        N'TRA_HANG'
    );

    COMMIT TRANSACTION;
    PRINT N'Migration completed successfully.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    PRINT N'Migration failed. Transaction rolled back.';
    THROW;
END CATCH;
GO
