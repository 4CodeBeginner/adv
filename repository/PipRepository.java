package repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import model.PenyaluranPip;
import model.ProvinsiSummary;

public interface PipRepository {
    List<ProvinsiSummary> findProvinsiSummaries(String keyword) throws SQLException;
    Map<String, long[]> getNasionalSummaryByJenjang() throws SQLException;
    List<PenyaluranPip> findByProvinsi(String provinsi) throws SQLException;
    PenyaluranPip findById(int id) throws SQLException;
    void insertBatchProvinsi(String provinsi, List<PenyaluranPip> list) throws SQLException;
    void insertSingleJenjang(PenyaluranPip item) throws SQLException;
    void updateSiswaOnly(int id, int siswaPemberian, int siswaAktivasi, int siswaRelaksasi) throws SQLException;
    long mutasiStatusSiswa(int id, int asalIdx, int tujuanIdx, long jumlahPindah) throws SQLException;
    void deleteById(int id) throws SQLException;
    int deleteByProvinsi(String provinsi) throws SQLException;
}