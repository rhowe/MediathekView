package mediathek.tool;

import mediathek.javafx.filterpanel.ZeitraumSpinner;
import org.apache.commons.configuration2.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class FilterConfiguration {
  public static final String FILTER_CONFIG_PROPERTIES_NAME_START = "filter.";
  public static final String FILTER_PANEL_CURRENT_FILTER = FILTER_CONFIG_PROPERTIES_NAME_START+"current.filter";
  protected static final String FILTER_PANEL_AVAILABLE_FILTERS = FILTER_CONFIG_PROPERTIES_NAME_START+"available.filters.filter_";
  protected static final String KEY_UUID_SPLITERATOR = "_";
  private static final Logger LOG = LoggerFactory.getLogger(FilterConfiguration.class);
  protected static final CopyOnWriteArraySet<Runnable> availableFiltersChangedCallbacks =
      new CopyOnWriteArraySet<>();
  protected static final CopyOnWriteArraySet<Consumer<FilterDTO>> currentFilterChangedCallbacks =
      new CopyOnWriteArraySet<>();
  protected final XMLConfiguration configuration;

  public FilterConfiguration() {
    this(ApplicationConfiguration.getConfiguration());
  }

  public FilterConfiguration(XMLConfiguration configuration) {
    super();
    this.configuration = configuration;
    migrateOldFilterConfigurations();
  }

  public static void addAvailableFiltersObserver(Runnable availableFiltersChangedCallback) {
    availableFiltersChangedCallbacks.add(availableFiltersChangedCallback);
  }

  public static void addCurrentFiltersObserver(Consumer<FilterDTO> currentFilterChangedCallback) {
    currentFilterChangedCallbacks.add(currentFilterChangedCallback);
  }

  private void migrateOldFilterConfigurations() {
    FilterDTO newFilter = new FilterDTO(UUID.randomUUID(), "Alter Filter");
    if (migrateAll(
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_ABOS.getOldKey(),
                newFilter,
                Boolean.class,
                this::setDontShowAbos),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_AUDIO_VERSIONS.getOldKey(),
                newFilter,
                Boolean.class,
                this::setDontShowAudioVersions),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_SIGN_LANGUAGE.getOldKey(),
                newFilter,
                Boolean.class,
                this::setDontShowSignLanguage),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_TRAILERS.getOldKey(),
                newFilter,
                Boolean.class,
                this::setDontShowTrailers),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_FILM_LENGTH_MAX.getOldKey(),
                newFilter,
                Double.class,
                this::setFilmLengthMax),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_FILM_LENGTH_MIN.getOldKey(),
                newFilter,
                Double.class,
                this::setFilmLengthMin),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_SHOW_HD_ONLY.getOldKey(),
                newFilter,
                Boolean.class,
                this::setShowHdOnly),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_SHOW_LIVESTREAMS_ONLY.getOldKey(),
                newFilter,
                Boolean.class,
                this::setShowLivestreamsOnly),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_SHOW_NEW_ONLY.getOldKey(),
                newFilter,
                Boolean.class,
                this::setShowNewOnly),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_SHOW_BOOK_MARKED_ONLY.getOldKey(),
                newFilter,
                Boolean.class,
                this::setShowBookMarkedOnly),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_SHOW_SUBTITLES_ONLY.getOldKey(),
                newFilter,
                Boolean.class,
                this::setShowSubtitlesOnly),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_SHOW_UNSEEN_ONLY.getOldKey(),
                newFilter,
                Boolean.class,
                this::setShowUnseenOnly),
        () ->
            migrateOldFilterConfiguration(
                FilterConfigurationKeys.FILTER_PANEL_ZEITRAUM.getOldKey(),
                newFilter,
                String.class,
                this::setZeitraum))) {
      addNewFilter(newFilter);
      LOG.info("Filter migration abgeschlossen.");
    }
  }

  @SafeVarargs
  private boolean migrateAll(Supplier<Boolean>... migrationSteps) {
    return !Arrays.stream(migrationSteps)
            .map(Supplier::get)
            .filter(Boolean::booleanValue).toList()
        .isEmpty();
  }

  private <T> boolean migrateOldFilterConfiguration(
      String oldFilterConfigKey,
      FilterDTO newFilter,
      Class<T> classOfValueType,
      Consumer<T> newFilterSetter) {
    if (configuration.containsKey(oldFilterConfigKey)) {
      LOG.info(
          "Alte Filter Konfiguration {} mit dem Wert {} gefunden. Migriere es zu einer neuen Filter Konfiguration mit der Filter ID {}.",
          oldFilterConfigKey,
          configuration.getString(oldFilterConfigKey),
          newFilter.id());
      setCurrentFilter(newFilter);
      T oldValue = configuration.get(classOfValueType, oldFilterConfigKey);
      if (oldValue == null) {
        LOG.info(
            "Filter Konfiguration {} ist null, ignoriere Konfiguration für Migration.",
            oldFilterConfigKey);
      } else {
        newFilterSetter.accept(oldValue);
        configuration.clearProperty(oldFilterConfigKey);
        return true;
      }
    }
    return false;
  }

  public boolean isShowHdOnly() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_HD_ONLY.getKey()),
        false);
  }

  public FilterConfiguration setShowHdOnly(boolean showHdOnly) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_HD_ONLY.getKey()),
        showHdOnly);
    return this;
  }

  protected String toFilterConfigNameWithCurrentFilter(String filterConfigNamePattern) {
    return String.format(filterConfigNamePattern, getCurrentFilterID());
  }

  public boolean isShowSubtitlesOnly() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_SUBTITLES_ONLY.getKey()),
        false);
  }

  public FilterConfiguration setShowSubtitlesOnly(boolean showSubtitlesOnly) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_SUBTITLES_ONLY.getKey()),
        showSubtitlesOnly);
    return this;
  }

  public boolean isShowNewOnly() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_NEW_ONLY.getKey()),
        false);
  }

  public FilterConfiguration setShowNewOnly(boolean showNewOnly) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_NEW_ONLY.getKey()),
        showNewOnly);
    return this;
  }

  public List<String> getSender() {
    return configuration.getList(
        String.class,
        toFilterConfigNameWithCurrentFilter(FilterConfigurationKeys.FILTER_PANEL_SENDER.getKey()),
        Collections.emptyList());
  }

  public FilterConfiguration setSender(List<String> sender) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(FilterConfigurationKeys.FILTER_PANEL_SENDER.getKey()),
        sender);
    return this;
  }

  public String getThema() {
    return configuration.getString(
        toFilterConfigNameWithCurrentFilter(FilterConfigurationKeys.FILTER_PANEL_THEMA.getKey()),
        "");
  }

  public FilterConfiguration setThema(String thema) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(FilterConfigurationKeys.FILTER_PANEL_THEMA.getKey()),
        thema);
    return this;
  }

  public boolean isShowBookMarkedOnly() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_BOOK_MARKED_ONLY.getKey()),
        false);
  }

  public FilterConfiguration setShowBookMarkedOnly(boolean showBookMarkedOnly) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_BOOK_MARKED_ONLY.getKey()),
        showBookMarkedOnly);
    return this;
  }

  public boolean isShowUnseenOnly() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_UNSEEN_ONLY.getKey()),
        false);
  }

  public FilterConfiguration setShowUnseenOnly(boolean showUnseenOnly) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_UNSEEN_ONLY.getKey()),
        showUnseenOnly);
    return this;
  }

  public boolean isShowLivestreamsOnly() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_LIVESTREAMS_ONLY.getKey()),
        false);
  }

  public FilterConfiguration setShowLivestreamsOnly(boolean showLivestreamsOnly) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_SHOW_LIVESTREAMS_ONLY.getKey()),
        showLivestreamsOnly);
    return this;
  }

  public boolean isDontShowAbos() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_ABOS.getKey()),
        false);
  }

  public FilterConfiguration setDontShowAbos(boolean dontShowAbos) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_ABOS.getKey()),
        dontShowAbos);
    return this;
  }

  public boolean isDontShowTrailers() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_TRAILERS.getKey()),
        false);
  }

  public FilterConfiguration setDontShowTrailers(boolean dontShowTrailers) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_TRAILERS.getKey()),
        dontShowTrailers);
    return this;
  }

  public boolean isDontShowSignLanguage() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_SIGN_LANGUAGE.getKey()),
        false);
  }

  public FilterConfiguration setDontShowSignLanguage(boolean dontShowSignLanguage) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_SIGN_LANGUAGE.getKey()),
        dontShowSignLanguage);
    return this;
  }

  public boolean isDontShowAudioVersions() {
    return configuration.getBoolean(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_AUDIO_VERSIONS.getKey()),
        false);
  }

  public FilterConfiguration setDontShowAudioVersions(boolean dontShowAudioVersions) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_DONT_SHOW_AUDIO_VERSIONS.getKey()),
        dontShowAudioVersions);
    return this;
  }

  public double getFilmLengthMin() {
    return configuration.getDouble(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_FILM_LENGTH_MIN.getKey()),
        0.0d);
  }

  public FilterConfiguration setFilmLengthMin(double filmLengthMin) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_FILM_LENGTH_MIN.getKey()),
        filmLengthMin);
    return this;
  }

  public double getFilmLengthMax() {
    return configuration.getDouble(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_FILM_LENGTH_MAX.getKey()),
        110.0d);
  }

  public FilterConfiguration setFilmLengthMax(double filmLengthMax) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(
            FilterConfigurationKeys.FILTER_PANEL_FILM_LENGTH_MAX.getKey()),
        filmLengthMax);
    return this;
  }

  public String getZeitraum() {
    return configuration.getString(
        toFilterConfigNameWithCurrentFilter(FilterConfigurationKeys.FILTER_PANEL_ZEITRAUM.getKey()),
        ZeitraumSpinner.UNLIMITED_VALUE);
  }

  public FilterConfiguration setZeitraum(String zeitraum) {
    configuration.setProperty(
        toFilterConfigNameWithCurrentFilter(FilterConfigurationKeys.FILTER_PANEL_ZEITRAUM.getKey()),
        zeitraum);
    return this;
  }

  public FilterConfiguration clearCurrentFilter() {
    Arrays.stream(FilterConfigurationKeys.values())
        .map(FilterConfigurationKeys::getKey)
        .map(this::toFilterConfigNameWithCurrentFilter)
        .forEach(configuration::clearProperty);
    return this;
  }

  public UUID getCurrentFilterID() {
    return getCurrentFilter().id();
  }

  public FilterDTO getCurrentFilter() {
    if (!configuration.containsKey(FILTER_CONFIG_PROPERTIES_NAME_START)
        || configuration.get(UUID.class, FILTER_CONFIG_PROPERTIES_NAME_START) == null) {
      setCurrentFilter(
          getAvailableFilters().stream()
              .findFirst()
              .orElseGet(
                  () -> {
                    FilterDTO newFilter = new FilterDTO(UUID.randomUUID(), "Filter 1");
                    addNewFilter(newFilter);
                    return newFilter;
                  }));
    }
    UUID currentFilterId = configuration.get(UUID.class, FILTER_CONFIG_PROPERTIES_NAME_START);
    return new FilterDTO(currentFilterId, getFilterName(currentFilterId));
  }

  public FilterConfiguration setCurrentFilter(FilterDTO currentFilter) {
    return setCurrentFilter(currentFilter.id());
  }

  public FilterConfiguration setCurrentFilter(UUID currentFilterID) {
    configuration.setProperty(FILTER_CONFIG_PROPERTIES_NAME_START, currentFilterID);
    currentFilterChangedCallbacks.forEach(consumer -> consumer.accept(getCurrentFilter()));
    return this;
  }

  public List<UUID> getAvailableFilterIds() {
    return getAvailableFilters().stream()
            .map(FilterDTO::id).toList();
  }

  public List<String> getAvailableFilterNames() {
    return getAvailableFilters().stream()
            .map(FilterDTO::name).toList();
  }

  public List<FilterDTO> getAvailableFilters() {
    List<String> availableFilterKeys = new ArrayList<>();
    configuration
        .getKeys()
        .forEachRemaining(
            key -> {
              if (key.startsWith(FILTER_PANEL_AVAILABLE_FILTERS)) {
                availableFilterKeys.add(key);
              }
            });
    return availableFilterKeys.stream()
            .map(
                    key ->
                            new FilterDTO(
                                    UUID.fromString(key.split(KEY_UUID_SPLITERATOR)[1]),
                                    configuration.getProperty(key).toString())).toList();
  }

  public String getFilterName(UUID id) {
    return getAvailableFilters().stream()
        .filter(filter -> filter.id().equals(id))
        .map(FilterDTO::name)
        .findFirst()
        .orElse("");
  }

  public FilterConfiguration addNewFilter(FilterDTO filterDTO) {
    configuration.addProperty(FILTER_PANEL_AVAILABLE_FILTERS + filterDTO.id(), filterDTO.name());
    availableFiltersChangedCallbacks.forEach(Runnable::run);
    return this;
  }

  public FilterConfiguration addNewFilter(UUID filterId, String filterName) {
    return addNewFilter(new FilterDTO(filterId, filterName));
  }

  public FilterConfiguration deleteFilter(FilterDTO filterToDelete) {
    return deleteFilter(filterToDelete.id());
  }

  public FilterConfiguration deleteFilter(UUID idToDelete) {
    boolean filterToDeleteIsCurrentFilter = idToDelete.equals(getCurrentFilterID());
    if (filterToDeleteIsCurrentFilter) {
      configuration.clearProperty(FILTER_CONFIG_PROPERTIES_NAME_START);
    }
    configuration
        .getKeys()
        .forEachRemaining(key -> clearPropertyWithKeyIfContainsId(idToDelete, key));
    availableFiltersChangedCallbacks.forEach(Runnable::run);
    if (filterToDeleteIsCurrentFilter) {
      currentFilterChangedCallbacks.forEach(consumer -> consumer.accept(getCurrentFilter()));
    }
    return this;
  }

  private void clearPropertyWithKeyIfContainsId(UUID idToDelete, String key) {
    if (key.contains(idToDelete.toString())) {
      configuration.clearProperty(key);
    }
  }

  public FilterConfiguration renameCurrentFilter(String newName) {
    configuration.setProperty(FILTER_PANEL_AVAILABLE_FILTERS + getCurrentFilterID(), newName);
    availableFiltersChangedCallbacks.forEach(Runnable::run);
    currentFilterChangedCallbacks.forEach(consumer -> consumer.accept(getCurrentFilter()));
    return this;
  }

  public Optional<FilterDTO> findFilterForName(String name) {
    return getAvailableFilters().stream().filter(filter -> filter.name().equals(name)).findFirst();
  }

  protected enum FilterConfigurationKeys {
    FILTER_PANEL_SHOW_HD_ONLY(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.show.hd_only"),
    FILTER_PANEL_SHOW_SUBTITLES_ONLY(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.show.subtitles_only"),
    FILTER_PANEL_SHOW_BOOK_MARKED_ONLY(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.show.book_marked_only"),
    FILTER_PANEL_SHOW_NEW_ONLY(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.show.new_only"),
    FILTER_PANEL_SHOW_UNSEEN_ONLY(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.show.unseen_only"),
    FILTER_PANEL_SHOW_LIVESTREAMS_ONLY(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.show.livestreams_only"),
    FILTER_PANEL_DONT_SHOW_ABOS(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.dont_show.abos"),
    FILTER_PANEL_DONT_SHOW_TRAILERS(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.dont_show.trailers"),
    FILTER_PANEL_DONT_SHOW_SIGN_LANGUAGE(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.dont_show.sign_language"),
    FILTER_PANEL_DONT_SHOW_AUDIO_VERSIONS(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.dont_show.audio_versions"),
    FILTER_PANEL_FILM_LENGTH_MIN(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.film_length.min"),
    FILTER_PANEL_SENDER(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.sender"),
    FILTER_PANEL_THEMA(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.thema"),
    FILTER_PANEL_FILM_LENGTH_MAX(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.film_length.max"),
    FILTER_PANEL_ZEITRAUM(FILTER_CONFIG_PROPERTIES_NAME_START+"filter_%s.zeitraum");

    private final String key;

    FilterConfigurationKeys(final String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }

    public String getOldKey() {
      return key.replace(".filter_%s", "");
    }
  }
}
