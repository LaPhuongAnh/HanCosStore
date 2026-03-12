-- Migration: Create YEU_CAU_DOI_TRA table for return/exchange requests
-- Run this on the SQL Server database before using the return feature

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'YEU_CAU_DOI_TRA')
BEGIN
    CREATE TABLE YEU_CAU_DOI_TRA (
        Id          INT IDENTITY(1,1) PRIMARY KEY,
        DonHangId   INT NOT NULL,
        TaiKhoanId  INT NOT NULL,
        LyDo        NVARCHAR(500) NOT NULL,
        TrangThai   NVARCHAR(30) NOT NULL DEFAULT N'PENDING',
        NgayTao     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_YeuCauDoiTra_DonHang FOREIGN KEY (DonHangId) REFERENCES DON_HANG(Id),
        CONSTRAINT FK_YeuCauDoiTra_TaiKhoan FOREIGN KEY (TaiKhoanId) REFERENCES TAI_KHOAN(Id)
    );
END
GO
