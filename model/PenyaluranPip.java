package model;

public class PenyaluranPip {
    private int id;
    private String provinsi;
    private String jenjang;
    private long siswaDisalurkan;
    private int siswaPemberian;
    private int siswaAktivasiNominasi;
    private int siswaRelaksasi;
    private long danaDisalurkan;
    private long danaPemberian;
    private long danaAktivasiNominasi;
    private long danaRelaksasi;

    public PenyaluranPip() {}

    public PenyaluranPip(String provinsi, String jenjang,
                         int siswaPemberian, int siswaAktivasiNominasi, int siswaRelaksasi,
                         long danaPemberian, long danaAktivasiNominasi, long danaRelaksasi) {
        this.provinsi = provinsi;
        this.jenjang = jenjang;
        this.siswaPemberian = siswaPemberian;
        this.siswaAktivasiNominasi = siswaAktivasiNominasi;
        this.siswaRelaksasi = siswaRelaksasi;
        this.danaPemberian = danaPemberian;
        this.danaAktivasiNominasi = danaAktivasiNominasi;
        this.danaRelaksasi = danaRelaksasi;
        hitungUlangTotal();
    }

    public void hitungUlangTotal() {
        this.siswaDisalurkan = (long) siswaPemberian + siswaAktivasiNominasi + siswaRelaksasi;
        this.danaDisalurkan = danaPemberian + danaAktivasiNominasi + danaRelaksasi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProvinsi() { return provinsi; }
    public void setProvinsi(String provinsi) { this.provinsi = provinsi; }

    public String getJenjang() { return jenjang; }
    public void setJenjang(String jenjang) { this.jenjang = jenjang; }

    public long getSiswaDisalurkan() { return siswaDisalurkan; }
    public void setSiswaDisalurkan(long siswaDisalurkan) { this.siswaDisalurkan = siswaDisalurkan; }

    public int getSiswaPemberian() { return siswaPemberian; }
    public void setSiswaPemberian(int siswaPemberian) { this.siswaPemberian = siswaPemberian; }

    public int getSiswaAktivasiNominasi() { return siswaAktivasiNominasi; }
    public void setSiswaAktivasiNominasi(int siswaAktivasiNominasi) { this.siswaAktivasiNominasi = siswaAktivasiNominasi; }

    public int getSiswaRelaksasi() { return siswaRelaksasi; }
    public void setSiswaRelaksasi(int siswaRelaksasi) { this.siswaRelaksasi = siswaRelaksasi; }

    public long getDanaDisalurkan() { return danaDisalurkan; }
    public void setDanaDisalurkan(long danaDisalurkan) { this.danaDisalurkan = danaDisalurkan; }

    public long getDanaPemberian() { return danaPemberian; }
    public void setDanaPemberian(long danaPemberian) { this.danaPemberian = danaPemberian; }

    public long getDanaAktivasiNominasi() { return danaAktivasiNominasi; }
    public void setDanaAktivasiNominasi(long danaAktivasiNominasi) { this.danaAktivasiNominasi = danaAktivasiNominasi; }

    public long getDanaRelaksasi() { return danaRelaksasi; }
    public void setDanaRelaksasi(long danaRelaksasi) { this.danaRelaksasi = danaRelaksasi; }
}