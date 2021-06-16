/*
 * Copyright (C) 2017 Carlos Olivo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package contingencia.Vista;

import contingencia.Archivo;
import contingencia.Modelo.Mercaderia;
import static contingencia.Util.esMercaderia;
import static contingencia.Util.verMercaderia;
import static contingencia.Util.mostrarConfirmacion;
import static contingencia.Util.mostrarError;
import static contingencia.Util.mostrarInfo;
import contingencia.Modelo.Remito;
import static contingencia.Util.esEntero;
import static contingencia.Util.redondear;
import static contingencia.Util.verVenta;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Clase grafica principal.
 * 
 * @author Carlos Olivo
 * @version 0.1
 */
public class MenuPrincipal extends Application {
  
  private Stage stage;
  private BorderPane bp;
  private TableView<Mercaderia> tablaMercaderias;
  private TableView<Remito> tablaRemitos;
  private ObservableList<Mercaderia> mercaderias;
  private ObservableList<Remito> remitos;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    validar();
  }
  
  /**
   * Valida que no sea un usuario inexistente.
   */
  private void validar() {
    if(stage.getUserData() == null) {
      mostrarError("Autentificaci\u00F3n requerida");
      stage.close();
    } else {
      cargar();
    }
  }
  
  /**
   * Prepara al escenario para iniciar.
   */
  private void cargar() {
    mercaderias = new Archivo<Mercaderia>().cargar("Mercaderia");
    remitos = new Archivo<Remito>().cargar("Remitos");
    stage.setTitle("Contingencia - Mercaderia");
    stage.getIcons().add(new Image("file:resources/images/cart.png"));
    stage.setResizable(false);
    stage.setScene(menu());
    //stage.getScene().getStylesheets().add("file:resources/DarkTheme.css");
    stage.centerOnScreen();
    stage.setOnCloseRequest((EventHandler) -> {
      if(esAdmin()) {
        guardar();
      }
    });
    stage.show();
  }
  
  /**
   * Guarda los Mercaderia; y las ventas.
   */
  private void guardar() {
    if(mercaderias!= null) {
      new Archivo<Mercaderia>().guardar(mercaderias, "Mercaderia");
    }
    if(remitos != null) {
      new Archivo<Remito>().guardar(remitos, "Remitos");
    }
  }
  
  /**
   * Crea la tabla Mercaderia;
   */
  public void crearTablaMercaderias() {
    tablaMercaderias = new TableView<>(mercaderias);
    tablaMercaderias.setPlaceholder(new Text("Sin Mercaderias..."));
    
    TableColumn clave = new TableColumn("Clave");
    clave.setCellValueFactory(new PropertyValueFactory("clave"));
    tablaMercaderias.getColumns().add(clave);
    
    TableColumn nombre = new TableColumn("Nombre");
    nombre.setCellValueFactory(new PropertyValueFactory("nombre"));
    tablaMercaderias.getColumns().add(nombre);
    
    TableColumn descripcion = new TableColumn("Descripci\u00F3n");
    descripcion.setCellValueFactory(new PropertyValueFactory("descripcion"));
    tablaMercaderias.getColumns().add(descripcion);
    
    TableColumn precio = new TableColumn("Precio");
    precio.setCellValueFactory(new PropertyValueFactory("precioCompra"));
    tablaMercaderias.getColumns().add(precio);
    
    TableColumn existencias = new TableColumn("Existencias");
    existencias.setCellValueFactory(new PropertyValueFactory("existencias"));
    tablaMercaderias.getColumns().add(existencias);
    
    TableColumn unidad = new TableColumn("Unidad");
    unidad.setCellValueFactory(new PropertyValueFactory("tipoUnidad"));
    tablaMercaderias.getColumns().add(unidad);
    
    tablaMercaderias.setRowFactory((TableView<Mercaderia> tableView) -> {
      final TableRow<Mercaderia> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      
      final MenuItem mostrar = new MenuItem("Mostrar");
      mostrar.setOnAction((ActionEvent) -> {
        verMercaderia(row.getItem());
      });
      contextMenu.getItems().add(mostrar);
      
      final MenuItem editar = new MenuItem("Editar");
      editar.setOnAction((ActionEvent) -> {
        editarMercaderia(row.getItem());
      });
      if(esAdmin()) {
        contextMenu.getItems().add(editar);
      }
      
      final MenuItem eliminar = new MenuItem("Eliminar");
      eliminar.setOnAction((ActionEvent) -> {
        eliminarMercaderia(row.getItem());
      });
      if(esAdmin()) {
        contextMenu.getItems().add(eliminar);
      }
      
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu)null)
              .otherwise(contextMenu)
      );
      return row ;
    });  
  }
  
  /**
   * Crea la tabla ventas.
   */
  private void crearTablaVentas() {
    tablaRemitos = new TableView<>(remitos);
    tablaRemitos.setPlaceholder(new Text("Sin ventas..."));
    
    TableColumn folio = new TableColumn("Folio");
    folio.setCellValueFactory(new PropertyValueFactory("folio"));
    tablaRemitos.getColumns().add(folio);
    
    TableColumn fecha = new TableColumn("Fecha");
    fecha.setCellValueFactory(new PropertyValueFactory("fecha"));
    tablaRemitos.getColumns().add(fecha);
    
    TableColumn mercaderia = new TableColumn("mercaderias");
    mercaderia.setCellValueFactory(new PropertyValueFactory("mercaderias"));
    tablaRemitos.getColumns().add(mercaderia);
    
    TableColumn cantidad = new TableColumn("Cantidad");
    cantidad.setCellValueFactory(new PropertyValueFactory("cantidad"));
    tablaRemitos.getColumns().add(cantidad);
    
    TableColumn subtotal = new TableColumn("Subtotal");
    subtotal.setCellValueFactory(new PropertyValueFactory("subtotal"));
    tablaRemitos.getColumns().add(subtotal);
    
    TableColumn iva = new TableColumn("IVA");
    iva.setCellValueFactory(new PropertyValueFactory("IVA"));
    tablaRemitos.getColumns().add(iva);
    
    TableColumn total = new TableColumn("Total");
    total.setCellValueFactory(new PropertyValueFactory("total"));
    tablaRemitos.getColumns().add(total);
    
    tablaRemitos.setRowFactory((TableView<Remito> tableView) -> {
      final TableRow<Remito> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      
      final MenuItem mostrar = new MenuItem("Mostrar");
      mostrar.setOnAction((ActionEvent) -> {
        verVenta(row.getItem());
      });
      contextMenu.getItems().add(mostrar);
      
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu)null)
              .otherwise(contextMenu)
      );
      return row ;
    });  
  }
  
  /**
   * Crea la escena principal.
   * @return Escena.
   */
  private Scene menu() {
    bp = new BorderPane();
    bp.setLeft(opciones());
    
    crearTablaMercaderias();
    crearTablaVentas();
    
    bp.setCenter(tablaMercaderias);
    
    return new Scene(bp, 1000, 500);
  }
  
  /**
   * Panel de opciones.
   * @return Panel.
   */
  private VBox opciones() {
    VBox vb = new VBox();
    vb.setPadding(new Insets(10));
    
    ComboBox<String> opciones = new ComboBox<>();
    opciones.getItems().addAll("Mercaderias", "Remitos");
    opciones.setValue("Mercaderias");
    opciones.setOnAction((ActionEvent) -> {
      switch(opciones.getSelectionModel().getSelectedItem()) {
        case "Mercaderias":
          stage.setTitle("Contingencia - Mercaderias");
          bp.setCenter(tablaMercaderias);
          bp.setBottom(null);
          break;
        case "Remitos":
          stage.setTitle("Contingencia - Remitos");
          bp.setCenter(tablaRemitos);
          bp.setBottom(filtrarFecha());
          break;
        default:
          mostrarError("Opci\u00F3n invalida");
      }
      bp.setRight(null);
    });
    vb.getChildren().add(opciones);
    
    Separator separador = new Separator();
    separador.setPadding(new Insets(5));
    vb.getChildren().add(separador);
    
    Text titulo1 = new Text("Mercaderias");
    titulo1.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    vb.getChildren().add(titulo1);
    
    Hyperlink agregar = new Hyperlink("Agregar");
    agregar.setOnAction((ActionEvent) -> {
      bp.setRight(agregarMercaderia());
    });
    if(esAdmin()) {
      vb.getChildren().add(agregar);
    }
   
    Hyperlink inventario = new Hyperlink("Inventario");
    inventario.setOnAction((ActionEvent) -> {
      calcularInventario();
    });
    vb.getChildren().add(inventario);
    
    Hyperlink buscar = new Hyperlink("Buscar");
    buscar.setOnAction((ActionEvent) -> {
      bp.setRight(buscarMercaderia());
    });
    vb.getChildren().add(buscar);
    
    Hyperlink vender = new Hyperlink("Vender");
    vender.setOnAction((ActionEvent) -> {
      bp.getLeft().setDisable(true);
      bp.setRight(venderMercaderia());
    });
    if(esAdmin()) {
      vb.getChildren().add(vender);
    }
    return vb;
  }
  
  /**
   * Panel para agregar un Mercaderia
   * @return Panel.
   */
  private VBox agregarMercaderia() {
    VBox vb = new VBox();
    vb.setPadding(new Insets(5));
    vb.setSpacing(5);
    vb.setAlignment(Pos.TOP_CENTER);
    
    Text titulo = new Text("Agregar Mercaderia");
    titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    vb.getChildren().add(titulo);
    
    TextField clave = new TextField();
    clave.setPromptText("Clave");
    vb.getChildren().add(clave);
    
    TextField nombre = new TextField();
    nombre.setPromptText("Nombre");
    vb.getChildren().add(nombre);
    
    TextField descripcion = new TextField();
    descripcion.setPromptText("Descripci\u00F3n");
    vb.getChildren().add(descripcion);
    
    TextField precio = new TextField();
    precio.setPromptText("Precio");
    vb.getChildren().add(precio);
    
    TextField existencias = new TextField();
    existencias.setPromptText("Existencias");
    vb.getChildren().add(existencias);
    
    TextField unidad = new TextField();
    unidad.setPromptText("Unidad");
    vb.getChildren().add(unidad);
    
    HBox hb = new HBox();
    hb.setSpacing(5);
    hb.setAlignment(Pos.CENTER);
    
    Button agregar = new Button("Agregar");
    agregar.setOnAction((ActionEvent) -> {
      if(!esMercaderia(clave, nombre, descripcion, precio, existencias, unidad)) {
        return;
      }
      if(existeMercaderia(Integer.parseInt(clave.getText()))) {
        mostrarError("Una Mercaderia con esta clave ya existe en el sistema.");
        clave.clear();
        return;
      }
      mercaderias.add(new Mercaderia(
          Integer.parseInt(clave.getText()),
          nombre.getText(),
          descripcion.getText(),
          Double.parseDouble(precio.getText()),
          Integer.parseInt(existencias.getText()),
          unidad.getText()
      ));
      clave.clear();
      nombre.clear();
      descripcion.clear();
      precio.clear();
      existencias.clear();
      unidad.clear();
    });
    hb.getChildren().add(agregar);
    
    Button cerrar = new Button("Cerrar");
    cerrar.setOnAction((ActionEvent) -> {
      bp.setRight(null);
    });
    hb.getChildren().add(cerrar);
    
    vb.getChildren().add(hb);
    return vb;
  }
  
  private void editarMercaderia(Mercaderia mercaderia) {
    EditarMercaderia eP = new EditarMercaderia();
    if(eP.cargar(stage, mercaderia)) {
      tablaMercaderias.refresh();
    }
  }
  
  /**
   * Panel para buscar un Mercaderia
   * @return Panel.
   */
  private VBox buscarMercaderia() {
    VBox vb = new VBox();
    vb.setPadding(new Insets(5));
    vb.setSpacing(5);
    vb.setAlignment(Pos.TOP_CENTER);
    
    Text titulo = new Text("Buscar Mercaderia");
    titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    vb.getChildren().add(titulo);
    
    ComboBox<String> opciones = new ComboBox<>();
    opciones.getItems().addAll("Clave", "Nombre", "Descripcion");
    opciones.setValue("Clave");
    vb.getChildren().add(opciones);
    
    TextField campo = new TextField();
    vb.getChildren().add(campo);
    
    HBox hb = new HBox();
    hb.setSpacing(5);
    hb.setAlignment(Pos.CENTER);
    
    Button agregar = new Button("Mostrar");
    agregar.setOnAction((ActionEvent) -> {
      if("".equals(campo.getText())) {
        mostrarInfo("Introduce un dato para continuar.");
        return;
      }
      switch(opciones.getSelectionModel().getSelectedItem()) {
        case "Clave":
          if(!esEntero(campo.getText())) {
            mostrarError("Introduce un n\u00FAmero entero valido.");
            return;
          }
          buscarMercaderiaClave(Integer.parseInt(campo.getText()));
          break;
        case "Nombre":
          buscarMercaderiaNombre(campo.getText());
          break;
        case "Descripcion":
          buscarMercaderiaDescripcion(campo.getText());
          break;
        default:
          mostrarError("Opci\u00F3n invalida");
          break;
      }
      campo.clear();
    });
    hb.getChildren().add(agregar);
    
    Button cerrar = new Button("Cerrar");
    cerrar.setOnAction((ActionEvent) -> {
      bp.setRight(null);
    });
    hb.getChildren().add(cerrar);
    
    vb.getChildren().add(hb);
    return vb;
  }
  
  /**
   * Elimina un Mercaderia del sistema.
   * @param mercaderia Mercaderia a eliminar.
   */
  private void eliminarMercaderia(Mercaderia mercaderia) {
    if(mostrarConfirmacion("Estas seguro de eliminar la Mercaderia con el ID #" + mercaderia.getClave() + "?")) {
      tablaMercaderias.getItems().remove(mercaderia);
    }
  }
  
  /**
   * Panel para vender una Mercaderia
   * @return Panel.
   */
  private VBox venderMercaderia() {
    VBox vb = new VBox();
    vb.setPadding(new Insets(5));
    vb.setSpacing(5);
    vb.setAlignment(Pos.TOP_CENTER);
    
    Text titulo = new Text("Remito Mercaderia");
    titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    vb.getChildren().add(titulo);
    
    TextField clave = new TextField();
    clave.setPromptText("Clave");
    vb.getChildren().add(clave);
    
    HBox hb = new HBox();
    hb.setSpacing(5);
    hb.setAlignment(Pos.CENTER);
    
    Button agregar = new Button("Agregar");
    hb.getChildren().add(agregar);
    
    Button listo = new Button("Listo");
    listo.setOnAction((ActionEvent) -> {
      if(cantidadP >= 1) {
        remitos.add(new Remito(generarFolio(), ventaP, cantidadP, redondear(subtotalP), 0.16));
      }
      existeP = false;
      cantidadP = 0;
      subtotalP = 0;
      ventaP = "";
      bp.getLeft().setDisable(false);
      bp.setRight(null);
      tablaMercaderias.refresh();
    });
    hb.getChildren().add(listo);
    
    vb.getChildren().add(hb);
    
    Label nota = new Label();
    vb.getChildren().add(nota);
    
    agregar.setOnAction((ActionEvent) -> {
      if(!esEntero(clave.getText())) {
        mostrarError("Introduce un n\u00FAmero entero valido.");
        return;
      }
      nota.setText(vendeMercaderia(Integer.parseInt(clave.getText())));
    });
    
    return vb;
  }
  
  protected boolean existeP = false;
  protected int cantidadP = 0;
  protected double subtotalP = 0;
  protected String ventaP = "";
  /**
   * Vende Mercaderias del inventario.
   * @param clave Clave del Mercaderia
   * @return Nota de venta
   */
  private String vendeMercaderia(int clave) {
    Iterator<Mercaderia> it = mercaderias.iterator();
    while(it.hasNext()) {
      existeP = false;
    Mercaderia mercaderia = it.next();
      if (mercaderia.getClave() == clave) {
        existeP = true;
        if(mercaderia.venderMercaderia()) {
          cantidadP++;
          double precio = mercaderia.getPrecioCompra() * 1.50;
          precio = redondear(precio);
          ventaP += mercaderia.getNombre() + " | " +mercaderia.getTipoUnidad() + " | $" + precio + "\n";
          subtotalP += precio;
        } else {
          mostrarError("La mercaderia no se encuentra en existencia.");
        }
        break;
      }
    }
    if(existeP == false) {
      mostrarError("La mercaderia no existe en el inventario.");
    }
    return ventaP;
  }
  
  /**
   * Comprueba si existe una mercaderias en el inventario.
   * @param clave Clave del mercaderias.
   * @return Verdadero si exista, falso en caso contrario.
   */
  private boolean existeMercaderia(int clave) {
    for (Mercaderia mercaderia : mercaderias) {
      if (mercaderia.getClave() == clave) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Calcula el valor del inventario.
   */
  private void calcularInventario() {
    int numMercaderias = 0;
    int numExistencias = 0;
    double costos = 0;
    for (Mercaderia mercaderia : mercaderias) {
      numMercaderias++;
      numExistencias += mercaderia.getExistencias();
      costos += mercaderia.getPrecioCompra();
    }
    String msg = "";
    msg += "Mercaderias: " + numMercaderias + "\n";
    msg += "Existencias: " + numExistencias + "\n";
    msg += "Valor: $" + costos;
    mostrarInfo(msg);
  }
  
  /**
   * Genera un folio para una venta.
   * @return Folio.
   */
  private int generarFolio() {
    int folio = 1;
    if(!remitos.isEmpty()) {
      folio += remitos.get(remitos.size() - 1).getFolio();
    }
    return folio;
  }
  
  /**
   * Comprueba si el usuario tiene permisos administrativos.
   * @return Verdadero si es administrador, falso en caso contrario.
   */
  private boolean esAdmin() {
    return stage.getUserData() == "true";
  }
  
   /**
   * Busca mercaderia en el inventario por su clave.
   * @param clave Clave de la mercaderia.
   */
  private void buscarMercaderiaClave(int clave) {
    for (Mercaderia mercaderia : mercaderias) {
      if (mercaderia.getClave() == clave) {
        verMercaderia(mercaderia);
        return;
      }
    }
    mostrarError("No existe mercaderia con tal clave en el inventario.");
  }
  
  /**
   * Busca un mercaderia en el inventario por su nombre.
   * @param nombre Nombre del mercaderia.
   */
  private void buscarMercaderiaNombre(String nombre) {
    for (Mercaderia mercaderia : mercaderias) {
      if (mercaderia.getNombre().equals(nombre)) {
        verMercaderia(mercaderia);
        return;
      }
    }
    mostrarError("No existe mercaderia con tal nombre en el inventario.");
  }
  
  /**
   * Busca un mercaderiao en el inventario por su descripción.
   * @param descripcion Descripción del mercaderia.
   */
  private void buscarMercaderiaDescripcion(String descripcion) {
    for (Mercaderia mercaderia : mercaderias) {
      if (mercaderia.getDescripcion().equals(descripcion)) {
        verMercaderia(mercaderia);
        return;
      }
    }
    mostrarError("No existe la mercaderia con tal descripcion en el inventario.");
  }
  
  /**
   * Panel para filtrar las ventas por fecha.
   * @return Panel.
   */
  private HBox filtrarFecha() {
    HBox hb = new HBox();
    hb.setSpacing(5);
    hb.setAlignment(Pos.CENTER_LEFT);
    
    FilteredList<Remito> filtro = new FilteredList<>(remitos);
    
    Label titulo = new Label("Filtrar por fecha ");
    hb.getChildren().add(titulo);
    
    ComboBox<String> opciones = new ComboBox<>();
    opciones.getItems().addAll("=", ">", "<");
    opciones.setValue("=");
    hb.getChildren().add(opciones);
    
    DatePicker fecha = new DatePicker();
    fecha.setEditable(false);
    fecha.setValue(LocalDate.now());
    hb.getChildren().add(fecha);
    
    Button filtrar = new Button("Filtrar");
    hb.getChildren().add(filtrar);
    
    Button restablecer = new Button("Restablecer");
    restablecer.setOnAction((ActionEvent) -> {
      opciones.setValue("=");
      fecha.setValue(LocalDate.now());
      restablecer.setVisible(false);
      tablaRemitos.setItems(remitos);
    });
    restablecer.setVisible(false);
    hb.getChildren().add(restablecer);
    
    filtrar.setOnAction((ActionEvent) -> {
      filtro.setPredicate(remito -> {
        LocalDate fechaV = LocalDate.parse(remito.getFecha(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        switch(opciones.getSelectionModel().getSelectedItem()) {
          case "=":
            return fechaV.isEqual(fecha.getValue());
          case ">":
            return fechaV.isAfter(fecha.getValue());
          case "<":
            return fechaV.isBefore(fecha.getValue());
        default:
          mostrarError("Opci\u00F3n invalida");
      }
        return true;
      });
      tablaRemitos.setItems(filtro);
      restablecer.setVisible(true);
    });
    
    return hb;
  }
  
}
