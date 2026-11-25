package model;

public class ProvinsiSummary {
    private final String provinsi;
    private final int jumlahJenjang;
    private final long totalPemberian;
    private final long totalAktivasi;
    private final long totalRelaksasi;
    private final long totalSiswa;
    private final long totalDana;

    public ProvinsiSummary(String provinsi, int jumlahJenjang,
                           long totalPemberian, long totalAktivasi, long totalRelaksasi,
                           long totalSiswa, long totalDana) {
        this.provinsi = provinsi;
        this.jumlahJenjang = jumlahJenjang;
        this.totalPemberian = totalPemberian;
        this.totalAktivasi = totalAktivasi;
        this.totalRelaksasi = totalRelaksasi;
        this.totalSiswa = totalSiswa;
        this.totalDana = totalDana;
    }

    public String getProvinsi() { return provinsi; }
    public int getJumlahJenjang() { return jumlahJenjang; }
    public long getTotalPemberian() { return totalPemberian; }
    public long getTotalAktivasi() { return totalAktivasi; }
    public long getTotalRelaksasi() { return totalRelaksasi; }
    public long getTotalSiswa() { return totalSiswa; }
    public long getTotalDana() { return totalDana; }
}