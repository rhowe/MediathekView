package mediathek.javafx.filterpanel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import mediathek.gui.messages.TableModelChangeEvent;
import mediathek.tool.FilterConfiguration;
import mediathek.tool.FilterDTO;
import mediathek.tool.MessageBus;
import net.engio.mbassy.listener.Handler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;

public class FXFilmToolBar extends ToolBar {
  @FXML public Button btnDownloadFilmList;

  @FXML public Button btnFilmInfo;

  @FXML Button btnPlay;

  @FXML Button btnRecord;

  @FXML Button btnBookmark;

  @FXML Button btnManageAbos;

  @FXML ToggleButton btnShowBookmarkedMovies;

  @FXML Button btnManageBookMarks;

  @FXML Button btnShowFilter;

  @FXML
  FXSearchControl jfxSearchField;

  @FXML ToggleButton btnSearchThroughDescription;

  @FXML ComboBox<FilterDTO> filterSelect;

  public FXFilmToolBar() {
    try {
      URL url = getClass().getResource("/mediathek/res/programm/fxml/film_toolbar.fxml");
      FXMLLoader fxmlLoader = new FXMLLoader(url);
      fxmlLoader.setRoot(this);
      fxmlLoader.setController(this);
      fxmlLoader.load();
      setUpFilterSelect();

      MessageBus.getMessageBus().subscribe(this);
    } catch (IOException e) {
      Logger logger = LogManager.getLogger(FXFilmToolBar.class);
      logger.error("Failed to load FXML!");
    }
  }

  /**
   * Temporary storage for search field focus when disabled
   */
  private boolean searchfieldFocused;

  @Handler
  private void handleTableModelChangeEvent(TableModelChangeEvent e) {
    if (e.active) {
      Platform.runLater(() -> {
        searchfieldFocused = jfxSearchField.isFocused();
        setDisable(true);
      });
    }
    else {
      Platform.runLater(() -> {
        setDisable(false);
        if (searchfieldFocused) {
          jfxSearchField.requestFocus();
          var searchText = jfxSearchField.getText();
          if (!searchText.isEmpty()) {
            jfxSearchField.positionCaret(searchText.length());
          }
        }
      });
    }
  }

  private void setUpFilterSelect() {
    FilterConfiguration filterConfig = new FilterConfiguration();
    ObservableList<FilterDTO> availableFilters =
        FXCollections.observableArrayList(filterConfig.getAvailableFilters());
    FilterConfiguration.addAvailableFiltersObserver(
        () -> {
          availableFilters.clear();
          availableFilters.addAll(filterConfig.getAvailableFilters());
        });

    SingleSelectionModel<FilterDTO> selectionModel = filterSelect.getSelectionModel();
    FilterConfiguration.addCurrentFiltersObserver(selectionModel::select);
    filterSelect.setItems(availableFilters);
    selectionModel.select(filterConfig.getCurrentFilter());
    selectionModel
        .selectedItemProperty()
        .addListener(
            (observableValue, oldValue, newValue) -> {
              if (newValue != null && !newValue.equals(filterConfig.getCurrentFilter())) {
                filterConfig.setCurrentFilter(newValue);
              }
            });
  }
}
