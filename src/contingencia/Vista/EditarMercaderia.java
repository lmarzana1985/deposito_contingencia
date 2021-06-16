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

import contingencia.Modelo.Mercaderia;
import static contingencia.Util.esMercaderia;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Clase encargada de editar un producto.
 * 
 * @author Carlos Olivo
 * @version 0.1
 */
public class EditarMercaderia extends Application {
  
  private Mercaderia mercaderia;
  private Stage stage;
  private TextField clave, nombre, descripcion, precio, existencias, unidad;
  private boolean guardarCambios = false;
  
  @Override
  public void start(Stage stage) {
    this.stage = stage;
  }
  
  /**
   * Carga el escenario con el producto a editar.
   * @param primaryStage Escenario.
   * @param producto Producto.
   * @return Verdadero si hubo cambios, falso en caso contrario.
   */
  public boolean cargar(Stage primaryStage, Mercaderia mercaderia) {
    this.mercaderia = mercaderia;
    stage = new Stage();
    stage.getIcons().add(new Image("file:resources/images/cart.png"));
    stage.setTitle("Editar producto");
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initOwner(primaryStage);
    stage.setResizable(false);
    stage.setScene(eP());
    stage.centerOnScreen();
    stage.showAndWait();
    return guardarCambios;
  }
  
  /**
   * Crea la escena para cargar la ventana de edición de producto.
   * @return Escena editar producto.
   */
  private Scene eP() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));
    
    Label claveL = new Label("Clave:");
    grid.add(claveL, 0, 0);
    
    clave = new TextField();
    clave.setText(Integer.toString(mercaderia.getClave()));
    clave.setDisable(true);
    grid.add(clave, 1, 0);
    
    Label nombreL = new Label("Nombre:");
    grid.add(nombreL, 0, 1);
    
    nombre = new TextField();
    nombre.setText(mercaderia.getNombre());
    grid.add(nombre, 1, 1);
    
    Label descripcionL = new Label("Descripci\u00F3n:");
    grid.add(descripcionL, 0, 2);
    
    descripcion = new TextField();
    descripcion.setText(mercaderia.getDescripcion());
    grid.add(descripcion, 1, 2);
    
    Label precioL = new Label("Precio:");
    grid.add(precioL, 0, 3);
    
    precio = new TextField();
    precio.setText(Double.toString(mercaderia.getPrecioCompra()));
    grid.add(precio, 1, 3);
    
    Label existenciasL = new Label("Existencias:");
    grid.add(existenciasL, 0, 4);
    
    existencias = new TextField();
    existencias.setText(Integer.toString(mercaderia.getExistencias()));
    grid.add(existencias, 1, 4);
    
    Label unidadL = new Label("Unidad:");
    grid.add(unidadL, 0, 5);
    
    unidad = new TextField();
    unidad.setText(mercaderia.getTipoUnidad());
    grid.add(unidad, 1, 5);
    
    HBox hb = new HBox();
    hb.setSpacing(10);
    hb.setAlignment(Pos.CENTER);
    
    Button guardar = new Button("Guardar");
    guardar.setOnAction((ActionEvent) -> {
      guardarMercaderia();
    });
    hb.getChildren().add(guardar);
    
    Button cerrar = new Button("Cancelar");
    cerrar.setOnAction((ActionEvent) -> {
      stage.close();
    });
    hb.getChildren().add(cerrar);
    
    grid.add(hb, 1, 6);
    return new Scene(grid, 265, 250);
  }
  
  /**
   * Guarda los cambios de un producto si es valido.
   */
  public void guardarMercaderia() {
    if(esMercaderia(clave, nombre, descripcion, precio, existencias, unidad)) {
     mercaderia.setNombre(nombre.getText());
      mercaderia.setDescripcion(descripcion.getText());
      mercaderia.setPrecioCompra(Double.parseDouble(precio.getText()));
      mercaderia.setExistencias(Integer.parseInt(existencias.getText()));
      mercaderia.setTipoUnidad(unidad.getText());
      guardarCambios = true;
      stage.close();
    }
  }  
}
