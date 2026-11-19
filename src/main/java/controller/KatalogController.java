package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import data.ProdukRepository;
import model.Produk;

public class KatalogController {

    private static final String DEFAULT_FOTO = "roti.png";

    @FXML
    private TextField namaField;
    @FXML
    private TextField hargaField;
    @FXML
    private ComboBox<String> fotoBox;
    @FXML
    private Label modeLabel;
    @FXML
    private Button simpanButton;
    @FXML
    private Button batalButton;
    @FXML
    private Label pesanLabel;
    @FXML
    private Label jumlahLabel;
    @FXML
    private TilePane katalogPane;
    private final ProdukRepository repository = new ProdukRepository();

    /** id produk yang sedang diedit; null berarti form dalam mode tambah. */
    private Long idDiedit = null;

    @FXML
    private void initialize() {
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");
        resetForm();
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();
        katalogPane.getChildren().clear();
        for (Produk produk : daftar) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }
        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);
        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());
        if (lokasi != null) {
            Image gambar = new Image(lokasi.toExternalForm(),
                    170, 110, true, true);
            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");
        Label hargaLabel = new Label("Rp" + produk.getHarga());
        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("button-edit");
        editButton.setOnAction(e -> startEdit(produk));
        Button hapusButton = new Button("Hapus");
        hapusButton.getStyleClass().add("button-danger");
        hapusButton.setOnAction(e -> deleteProduct(produk));
        HBox aksi = new HBox(8, editButton, hapusButton);
        aksi.setAlignment(Pos.CENTER_LEFT);

        VBox kartu = new VBox();
        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");
        if (idDiedit != null && idDiedit == produk.getId()) {
            kartu.getStyleClass().add("kartu-diedit");
        }
        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);
        kartu.getChildren().add(aksi);
        return kartu;
    }

    /**
     * Tombol Simpan / Simpan Perubahan.
     * Mode tambah -> INSERT, mode edit -> UPDATE.
     */
    @FXML
    private void saveProduct() {
        Produk produk;
        try {
            String nama = namaField.getText();
            long harga = Long.parseLong(hargaField.getText().trim());
            String foto = fotoBox.getValue();
            long id = idDiedit == null ? 0 : idDiedit;
            produk = new Produk(id, nama, harga, foto);
        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus angka bulat, contoh 12000.");
            return;
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
            return;
        }

        boolean modeEdit = idDiedit != null;
        String pesanSukses;
        try {
            if (modeEdit) {
                if (!repository.update(produk)) {
                    resetForm();
                    showData();
                    pesanLabel.setText("Produk sudah tidak ada di database.");
                    return;
                }
                pesanSukses = "Perubahan produk ID " + produk.getId() + " berhasil disimpan.";
            } else {
                repository.add(produk);
                pesanSukses = "Produk berhasil disimpan.";
            }
        } catch (SQLException e) {
            pesanLabel.setText("Gagal menyimpan. Lihat Output.");
            e.printStackTrace();
            return;
        }

        resetForm();
        try {
            showData();
            pesanLabel.setText(pesanSukses);
        } catch (SQLException e) {
            pesanLabel.setText("Data tersimpan, tetapi katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
    }

    /** Tombol Edit di kartu: isi form dengan data produk lalu masuk mode edit. */
    private void startEdit(Produk produk) {
        idDiedit = produk.getId();
        namaField.setText(produk.getNama());
        hargaField.setText(String.valueOf(produk.getHarga()));
        if (!fotoBox.getItems().contains(produk.getFoto())) {
            fotoBox.getItems().add(produk.getFoto());
        }
        fotoBox.setValue(produk.getFoto());
        modeLabel.setText("Mode edit: " + produk.getNama() + " (ID " + produk.getId() + ")");
        modeLabel.setVisible(true);
        modeLabel.setManaged(true);
        simpanButton.setText("Simpan Perubahan");
        pesanLabel.setText("Ubah data lalu klik Simpan Perubahan, atau Batal.");
        namaField.requestFocus();
        refreshQuietly();
    }

    /** Tombol Batal: buang isian form dan keluar dari mode edit. */
    @FXML
    private void cancelEdit() {
        boolean tadinyaEdit = idDiedit != null;
        resetForm();
        refreshQuietly();
        pesanLabel.setText(tadinyaEdit ? "Edit dibatalkan. Data tidak diubah." : "Form dikosongkan.");
    }

    /** Tombol Hapus di kartu: minta konfirmasi lalu DELETE dari database. */
    private void deleteProduct(Produk produk) {
        ButtonType hapus = new ButtonType("Hapus", ButtonBar.ButtonData.OK_DONE);
        ButtonType batal = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION,
                "Produk \"" + produk.getNama() + "\" (ID " + produk.getId()
                + ") akan dihapus permanen dari database.", hapus, batal);
        konfirmasi.setTitle("Hapus Produk");
        konfirmasi.setHeaderText("Hapus produk ini?");
        if (katalogPane.getScene() != null) {
            konfirmasi.initOwner(katalogPane.getScene().getWindow());
        }
        Optional<ButtonType> pilihan = konfirmasi.showAndWait();
        if (pilihan.isEmpty() || pilihan.get() != hapus) {
            pesanLabel.setText("Hapus dibatalkan.");
            return;
        }

        try {
            boolean terhapus = repository.delete(produk.getId());
            if (idDiedit != null && idDiedit == produk.getId()) {
                resetForm();
            }
            showData();
            pesanLabel.setText(terhapus
                    ? "Produk \"" + produk.getNama() + "\" berhasil dihapus."
                    : "Produk sudah tidak ada di database.");
        } catch (SQLException e) {
            pesanLabel.setText("Gagal menghapus. Lihat Output.");
            e.printStackTrace();
        }
    }

    /** Kembalikan form ke mode tambah dalam keadaan kosong. */
    private void resetForm() {
        idDiedit = null;
        namaField.clear();
        hargaField.clear();
        fotoBox.setValue(DEFAULT_FOTO);
        modeLabel.setText("");
        modeLabel.setVisible(false);
        modeLabel.setManaged(false);
        simpanButton.setText("Simpan");
    }

    /** Muat ulang katalog (untuk menyorot kartu yang sedang diedit). */
    private void refreshQuietly() {
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }
}
