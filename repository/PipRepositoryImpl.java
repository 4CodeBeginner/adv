package repository;

import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.PenyaluranPip;
import model.ProvinsiSummary;

public class PipRepositoryImpl implements PipRepository {

    @Override
    public List<ProvinsiSummary> findProvinsiSummaries(String keyword) throws SQLException {
        List<ProvinsiSummary> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT provinsi, COUNT(DISTINCT jenjang) AS jml_jenjang, "
                        + "SUM(siswa_pemberian) AS tot_pemberian, "
                        + "SUM(siswa_aktivasi_nominasi) AS tot_aktivasi, "
                        + "SUM(siswa_relaksasi) AS tot_relaksasi, "
                        + "SUM(siswa_disalurkan) AS tot_siswa, "
                        + "SUM(dana_disalurkan) AS tot_dana "
                        + "FROM penyaluran_pip "
        );

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("WHERE provinsi LIKE ? ");
        }
        sql.append("GROUP BY provinsi ORDER BY MIN(id) ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(1, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProvinsiSummary(
                            rs.getString("provinsi"),
                            rs.getInt("jml_jenjang"),
                            rs.getLong("tot_pemberian"),
                            rs.getLong("tot_aktivasi"),
                            rs.getLong("tot_relaksasi"),
                            rs.getLong("tot_siswa"),
                            rs.getLong("tot_dana")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public Map<String, long[]> getNasionalSummaryByJenjang() throws SQLException {
        Map<String, long[]> map = new HashMap<>();
        String sql = "SELECT jenjang, SUM(siswa_disalurkan) AS tot_siswa, SUM(dana_disalurkan) AS tot_dana "
                + "FROM penyaluran_pip GROUP BY jenjang";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("jenjang"), new long[]{
                        rs.getLong("tot_siswa"),
                        rs.getLong("tot_dana")
                });
            }
        }
        return map;
    }

    @Override
    public List<PenyaluranPip> findByProvinsi(String provinsi) throws SQLException {
        List<PenyaluranPip> list = new ArrayList<>();
        String sql = "SELECT * FROM penyaluran_pip WHERE provinsi = ? "
                + "ORDER BY FIELD(jenjang, 'SD', 'SMP', 'SMA', 'SMK'), id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provinsi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public PenyaluranPip findById(int id) throws SQLException {
        String sql = "SELECT * FROM penyaluran_pip WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        throw new SQLException("Data dengan ID " + id + " tidak ditemukan.");
    }

    @Override
    public void insertBatchProvinsi(String provinsi, List<PenyaluranPip> list) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            try (PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT COUNT(*) FROM penyaluran_pip WHERE provinsi = ?")) {
                checkPs.setString(1, provinsi);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalStateException("Provinsi \"" + provinsi + "\" sudah terdaftar di database!");
                    }
                }
            }

            conn.setAutoCommit(false);
            String sql = "INSERT INTO penyaluran_pip (provinsi, jenjang, siswa_disalurkan, "
                    + "siswa_pemberian, siswa_aktivasi_nominasi, siswa_relaksasi, "
                    + "dana_disalurkan, dana_pemberian, dana_aktivasi_nominasi, dana_relaksasi) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (PenyaluranPip item : list) {
                    bindInsertStatement(ps, item);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void insertSingleJenjang(PenyaluranPip item) throws SQLException {
        String sql = "INSERT INTO penyaluran_pip (provinsi, jenjang, siswa_disalurkan, "
                + "siswa_pemberian, siswa_aktivasi_nominasi, siswa_relaksasi, "
                + "dana_disalurkan, dana_pemberian, dana_aktivasi_nominasi, dana_relaksasi) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindInsertStatement(ps, item);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateSiswaOnly(int id, int siswaPemberian, int siswaAktivasi, int siswaRelaksasi) throws SQLException {
        long totalSiswa = (long) siswaPemberian + siswaAktivasi + siswaRelaksasi;
        String sql = "UPDATE penyaluran_pip SET siswa_disalurkan=?, siswa_pemberian=?, "
                + "siswa_aktivasi_nominasi=?, siswa_relaksasi=? WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, totalSiswa);
            ps.setInt(2, siswaPemberian);
            ps.setInt(3, siswaAktivasi);
            ps.setInt(4, siswaRelaksasi);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    @Override
    public long mutasiStatusSiswa(int id, int asalIdx, int tujuanIdx, long jumlahPindah) throws SQLException {
        PenyaluranPip current = findById(id);

        long[] siswaArr = {
                current.getSiswaPemberian(),
                current.getSiswaAktivasiNominasi(),
                current.getSiswaRelaksasi()
        };
        long[] danaArr = {
                current.getDanaPemberian(),
                current.getDanaAktivasiNominasi(),
                current.getDanaRelaksasi()
        };

        if (jumlahPindah > siswaArr[asalIdx]) {
            throw new IllegalArgumentException("Jumlah siswa dipindahkan melebihi stok siswa pada kategori asal.");
        }

        long danaMutasi = (jumlahPindah == siswaArr[asalIdx])
                ? danaArr[asalIdx]
                : Math.round(((double) danaArr[asalIdx] / siswaArr[asalIdx]) * jumlahPindah);

        siswaArr[asalIdx] -= jumlahPindah;
        danaArr[asalIdx] -= danaMutasi;

        siswaArr[tujuanIdx] += jumlahPindah;
        danaArr[tujuanIdx] += danaMutasi;

        long totalSiswa = siswaArr[0] + siswaArr[1] + siswaArr[2];
        long totalDana = danaArr[0] + danaArr[1] + danaArr[2];

        String sql = "UPDATE penyaluran_pip SET "
                + "siswa_disalurkan=?, siswa_pemberian=?, siswa_aktivasi_nominasi=?, siswa_relaksasi=?, "
                + "dana_disalurkan=?, dana_pemberian=?, dana_aktivasi_nominasi=?, dana_relaksasi=? "
                + "WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, totalSiswa);
            ps.setLong(2, siswaArr[0]);
            ps.setLong(3, siswaArr[1]);
            ps.setLong(4, siswaArr[2]);
            ps.setLong(5, totalDana);
            ps.setLong(6, danaArr[0]);
            ps.setLong(7, danaArr[1]);
            ps.setLong(8, danaArr[2]);
            ps.setInt(9, id);
            ps.executeUpdate();
        }
        return danaMutasi;
    }

    @Override
    public void deleteById(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM penyaluran_pip WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public int deleteByProvinsi(String provinsi) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM penyaluran_pip WHERE provinsi = ?")) {
            ps.setString(1, provinsi);
            return ps.executeUpdate();
        }
    }

    private void bindInsertStatement(PreparedStatement ps, PenyaluranPip item) throws SQLException {
        ps.setString(1, item.getProvinsi());
        ps.setString(2, item.getJenjang());
        ps.setLong(3, item.getSiswaDisalurkan());
        ps.setInt(4, item.getSiswaPemberian());
        ps.setInt(5, item.getSiswaAktivasiNominasi());
        ps.setInt(6, item.getSiswaRelaksasi());
        ps.setLong(7, item.getDanaDisalurkan());
        ps.setLong(8, item.getDanaPemberian());
        ps.setLong(9, item.getDanaAktivasiNominasi());
        ps.setLong(10, item.getDanaRelaksasi());
    }

    private PenyaluranPip mapRow(ResultSet rs) throws SQLException {
        PenyaluranPip p = new PenyaluranPip();
        p.setId(rs.getInt("id"));
        p.setProvinsi(rs.getString("provinsi"));
        p.setJenjang(rs.getString("jenjang"));
        p.setSiswaDisalurkan(rs.getLong("siswa_disalurkan"));
        p.setSiswaPemberian(rs.getInt("siswa_pemberian"));
        p.setSiswaAktivasiNominasi(rs.getInt("siswa_aktivasi_nominasi"));
        p.setSiswaRelaksasi(rs.getInt("siswa_relaksasi"));
        p.setDanaDisalurkan(rs.getLong("dana_disalurkan"));
        p.setDanaPemberian(rs.getLong("dana_pemberian"));
        p.setDanaAktivasiNominasi(rs.getLong("dana_aktivasi_nominasi"));
        p.setDanaRelaksasi(rs.getLong("dana_relaksasi"));
        return p;
    }
}