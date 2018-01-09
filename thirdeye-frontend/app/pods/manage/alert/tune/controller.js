/**
 * Controller for Alert Details Page: Tune Sensitivity Tab
 * @module manage/alert/tune
 * @exports manage/alert/tune
 */
import Controller from '@ember/controller';
import moment from 'moment';
import { computed } from '@ember/object';
import { buildDateEod } from 'thirdeye-frontend/helpers/utils';
import { buildAnomalyStats } from 'thirdeye-frontend/helpers/manage-alert-utils';

export default Controller.extend({
  /**
   * Be ready to receive time span for anomalies via query params
   */
  queryParams: ['duration', 'startDate', 'endDate'],
  duration: null,
  startDate: null,
  endDate: null,

  /**
   * Set initial view values
   */
  filterBy: 'All',
  isGraphReady: false,
  isTunePreviewActive: false,
  isTuneSaveSuccess: false,
  isTuneSaveFailure: false,
  selectedSeverityOption: 'Percentage of Change',
  selectedTunePattern: 'None',
  customPercentChange: '30',
  selectedTuneType: 'current',
  customMttdChange: '5',
  predefinedRanges: {},
  today: moment(),
  selectedSortMode: '',
  sortColumnStartUp: false,
  sortColumnScoreUp: false,
  sortColumnChangeUp: false,
  sortColumnResolutionUp: false,

  /**
   * Severity display options (power-select) and values
   */
  severityMap: {
    'Percentage of Change': 'weight',
    'Absolute Value of Change': 'deviation',
    'Site Wide Impact': 'site_wide_impact'
  },

  tuneSeverityOptions: computed('severityMap', function() {
    return Object.keys(this.get('severityMap'));
  }),

  /**
   * Pattern display options (power-select) and values
   */
  patternMap: {
    'None': '',
    'Up and Down': 'UP,DOWN',
    'Up Only': 'UP',
    'Down Only': 'DOWN'
  },

  tunePatternOptions: computed('patternMap', function() {
    return Object.keys(this.get('patternMap'));
  }),

  /**
   * Mapping anomaly table column names to corresponding prop keys
   */
  sortMap: {
    start: 'anomalyStart',
    score: 'severityScore',
    change: 'changeRate',
    resolution: 'anomalyFeedback'
  },

  /**
   * Builds the new autotune filter from custom tuning options
   * @type {String}
   */
  customTuneQueryString: computed(
    'selectedSeverityOption',
    'customPercentChange',
    'customMttdChange',
    'selectedTunePattern',
    function() {
      const severityMap = this.get('severityMap');
      const patternMap = this.get('patternMap');
      const selectedPattern = this.get('selectedTunePattern');
      const selectedSeverity = this.get('selectedSeverityOption');
      const mttdVal = Number(this.get('customMttdChange')).toFixed(2);
      const severityThresholdVal = (Number(this.get('customPercentChange'))/100).toFixed(2);
      const featureString = `window_size_in_hour,${severityMap[selectedSeverity]}`;
      const mttdString = `window_size_in_hour=${mttdVal};${severityMap[selectedSeverity]}=${severityThresholdVal}`;
      const patternString = patternMap[selectedPattern] ? `&pattern=${encodeURIComponent(patternMap[selectedPattern])}` : '';
      return `&features=${encodeURIComponent(featureString)}&mttd=${encodeURIComponent(mttdString)}${patternString}`;
    }
  ),

  /**
   * Indicates the allowed date range picker increment based on granularity
   * @type {Number}
   */
  timePickerIncrement: computed('alertData.windowUnit', function() {
    const granularity = this.get('alertData.windowUnit').toLowerCase();

    switch(granularity) {
      case 'days':
        return 1440;
      case 'hours':
        return 60;
      default:
        return 5;
    }
  }),

  /**
   * Allows us to enable/disable the custom tuning options
   * @type {Boolean}
   */
  isCustomFieldsDisabled: computed('selectedTuneType', function() {
    return this.get('selectedTuneType') === 'current';
  }),

  /**
   * date-time-picker: indicates the date format to be used based on granularity
   * @type {String}
   */
  uiDateFormat: computed('alertData.windowUnit', function() {
    const granularity = this.get('alertData.windowUnit').toLowerCase();

    switch(granularity) {
      case 'days':
        return 'MMM D, YYYY';
      case 'hours':
        return 'MMM D, YYYY h a';
      default:
        return 'MMM D, YYYY hh:mm a';
    }
  }),

  /**
   * date-time-picker: returns a time object from selected range end date
   * @type {Object}
   */
  viewRegionEnd: computed(
    'activeRangeEnd',
    function() {
      return moment(this.get('activeRangeEnd')).format(this.get('serverDateFormat'));
    }
  ),

  /**
   * date-time-picker: returns a time object from selected range start date
   * @type {Object}
   */
  viewRegionStart: computed(
    'activeRangeStart',
    function() {
      return moment(this.get('activeRangeStart')).format(this.get('serverDateFormat'));
    }
  ),

  /**
   * Data needed to render the stats 'cards' above the anomaly graph for this alert
   * @type {Object}
   */
  anomalyStats: computed(
    'alertEvalMetrics',
    'alertEvalMetrics.projected',
    function() {
      const evalMetrics = this.get('alertEvalMetrics');
      return buildAnomalyStats(evalMetrics, 'tune');
    }
  ),

  /**
   * Data needed to render the stats 'cards' above the anomaly graph for this alert
   * @type {Object}
   */
  diffedAnomalies: computed(
    'anomalyData',
    'filterBy',
    'selectedSortMode',
    function() {
      const {
        anomalyData: anomalies,
        filterBy: activeFilter,
        selectedSortMode
      } = this.getProperties('anomalyData', 'filterBy', 'selectedSortMode');
      let filterKey = '';
      let filteredAnomalies = anomalies;

      switch (activeFilter) {
        case 'True Anomalies':
          filterKey = 'True Anomaly';
          break;
        case 'False Alarms':
          filterKey = 'False Alarm';
          break;
        case 'User Created':
          filterKey = 'New Trend';
          break;
        default:
          filterKey = ''
      };

      // Filter anomalies in table according to filterkey
      if (activeFilter !== 'All') {
        filteredAnomalies = anomalies.filter(anomaly => anomaly.anomalyFeedback === filterKey);
      }
      if (selectedSortMode) {
        let [ sortKey, sortDir ] = selectedSortMode.split(':');
        if (sortDir === 'up') {
          filteredAnomalies = filteredAnomalies.sortBy(this.get('sortMap')[sortKey]);
        } else {
          filteredAnomalies = filteredAnomalies.sortBy(this.get('sortMap')[sortKey]).reverse();
        }
      }

      return filteredAnomalies;
    }
  ),

  /**
   * Reset the controller values on exit
   * @method clearAll
   */
  clearAll() {
    this.setProperties({
      alertEvalMetrics: {}
    });
  },

  actions: {

    /**
     * Trigger reload in model with new time range. Transition for 'custom' dates is handled by 'onRangeSelection'
     * @method onRangeOptionClick
     * @param {Object} rangeOption - the selected range object
     */
    onRangeOptionClick(rangeOption) {
      const rangeFormat = 'YYYY-MM-DD';
      const defaultEndDate = buildDateEod(1, 'day').valueOf();
      const timeRangeOptions = this.get('timeRangeOptions');

      if (rangeOption.value !== 'custom') {
        // Set date picker defaults to new start/end dates
        this.setProperties({
          activeRangeStart: moment(rangeOption.start).format(rangeFormat),
          activeRangeEnd: moment(defaultEndDate).format(rangeFormat)
        });
        // Reset options and highlight selected one
        timeRangeOptions.forEach(op => Ember.set(op, 'isActive', false));
        Ember.set(rangeOption, 'isActive', true);
        // Reload model according to new timerange
        this.transitionToRoute({ queryParams: {
          duration: rangeOption.value,
          startDate: rangeOption.start,
          endDate: buildDateEod(1, 'day').valueOf()
        }});
      }
    },

    /**
     * Sets the new custom date range for anomaly coverage
     * @method onRangeSelection
     * @param {String} start  - stringified start date
     * @param {String} end    - stringified end date
     */
    onRangeSelection(start, end) {
      const timeRangeOptions = this.get('timeRangeOptions');
      const currOption = timeRangeOptions.find(option => option.value === 'custom');
      // Toggle time reange button states to highlight the current one
      timeRangeOptions.forEach(op => Ember.set(op, 'isActive', false));
      Ember.set(currOption, 'isActive', true);
      // Reload model according to new timerange
      this.transitionToRoute({ queryParams: {
        mode: 'explore',
        duration: 'custom',
        startDate: moment(start).valueOf(),
        endDate: moment(end).valueOf()
      }});
    },

    /**
     * Save the currently loaded tuning options
     * @method onSubmitTuning
     */
    onSubmitTuning() {
      const tuneId = this.get('alertEvalMetrics.autotuneId');
      this.send('submitTuningRequest', tuneId);
    },

    /**
     * Enable preview button
     */
    onUpdateTuneType() {
      // TODO: if needed, handle enable/disable of preview button here
      // this.set('isPreviewDisabled', false);
    },

    /**
     * Handle "reset" click - reload the model
     */
    onResetPage() {
      this.set('isTunePreviewActive', false);
      this.send('resetPage');
    },

    /**
     * Replaces the 'tableStats' object with a new one with selected filter
     * activated and triggers table filtering.
     * @param {String} metric - label of the currently selected category
     */
    toggleCategory(metric) {
      const stats = this.get('tableStats');
      const newStats = stats.map((cat) => {
        return {
          count: cat.count,
          label: cat.label,
          isActive: false
        };
      });
      // Activate selected metric in our new stats object
      newStats.find(cat => cat.label === metric).isActive = true;
      // Apply new table stats object and trigger re-render of filtered anomalies
      this.setProperties({
        tableStats: newStats,
        filterBy: metric
      });
    },

    /**
     * Handle sorting for each sortable table column
     * @param {String} sortKey  - stringified start date
     */
    toggleSortDirection(sortKey) {
      const propName = `sortColumn${sortKey.capitalize()}Up` || '';

      this.toggleProperty(propName);
      if (this.get(propName)) {
        this.set('selectedSortMode', `${sortKey}:up`);
      } else {
        this.set('selectedSortMode', `${sortKey}:down`);
      }
      //On sort, set table to first pagination page
      this.set('currentPage', 1);
    },

    /**
     * On "preview" click, display the resulting anomaly table and trigger
     * tuning if we have custom settings (tuning data for default option is already loaded)
     */
    onClickPreviewPerformance() {
      const isTuneTypeCustom = this.get('selectedTuneType') === 'custom';
      this.set('isTunePreviewActive', true);
      if (isTuneTypeCustom) {
        this.send('triggerTuningSequence', this.get('customTuneQueryString'));
      }
    }
  }

});
