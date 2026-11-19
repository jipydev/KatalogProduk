package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import db.Database;
import model.Produk;

/**
 * Semua operasi CRUD tabel produk di SQLite (data/katalog.db).
 */
public class ProdukRepository {

    /** CREATE: menambah produk baru. */
    public void add(Produk produk) throws SQLException {
        String sql = "INSERT INTO produk (nama, harga, foto) VALUES (?, ?, ?)";
        try (
            Connection koneksi = Database.bukaKoneksi();
            PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, produk.getNama());
            perintah.setLong(2, produk.getHarga());
            perintah.setString(3, produk.getFoto());
            perintah.executeUpdate();
        }
    }

    /** READ: mengambil semua produk, urut berdasarkan id. */
    public ArrayList<Produk> getAll() throws SQLException {
        ArrayList<Produk> daftarProduk = new ArrayList<Produk>();
        String sql = "SELECT id, nama, harga, foto FROM produk ORDER BY id";
        try (
            Connection koneksi = Database.bukaKoneksi();
            PreparedStatement perintah = koneksi.prepareStatement(sql);
            ResultSet hasil = perintah.executeQuery()) {
            while (hasil.next()) {
                daftarProduk.add(bacaBaris(hasil));
            }
        }
        return daftarProduk;
    }

    /** READ: mengambil satu produk berdasarkan id, atau null kalau tidak ada. */
    public Produk getById(long id) throws SQLException {
        String sql = "SELECT id, nama, harga, foto FROM produk WHERE id = ?";
        try (
            Connection koneksi = Database.bukaKoneksi();
            PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setLong(1, id);
            try (ResultSet hasil = perintah.executeQuery()) {
                return hasil.next() ? bacaBaris(hasil) : null;
            }
        }
    }

    /**
     * UPDATE: mengubah nama, harga, dan foto produk dengan id yang sama.
     *
     * @return true kalau ada baris yang berubah, false kalau id tidak ditemukan.
     */
    public boolean update(Produk produk) throws SQLException {
        String sql = "UPDATE produk SET nama = ?, harga = ?, foto = ? WHERE id = ?";
        try (
            Connection koneksi = Database.bukaKoneksi();
            PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, produk.getNama());
            perintah.setLong(2, produk.getHarga());
            perintah.setString(3, produk.getFoto());
            perintah.setLong(4, produk.getId());
            return perintah.executeUpdate() > 0;
        }
    }

    /**
     * DELETE: menghapus produk berdasarkan id.
     *
     * @return true kalau produk terhapus, false kalau id tidak ditemukan.
     */
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM produk WHERE id = ?";
        try (
            Connection koneksi = Database.bukaKoneksi();
            PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setLong(1, id);
            return perintah.executeUpdate() > 0;
        }
    }

    private Produk bacaBaris(ResultSet hasil) throws SQLException {
        long id = hasil.getLong("id");
        String nama = hasil.getString("nama");
        long harga = hasil.getLong("harga");
        String foto = hasil.getString("foto");
        return new Produk(id, nama, harga, foto);
    }
}
