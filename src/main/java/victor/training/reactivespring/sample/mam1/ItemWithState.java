package victor.training.reactivespring.sample.mam1;

class ItemWithState {
      final MasterItem kafkaItem;
      final AnalyserItem persistentItem;
      final ItemState state;

      /**
       * If there is already an entry in the AnalyserItem table, use it to determine the state.
       */
      ItemWithState(MasterItem kafkaItem, AnalyserItem persistentItem) {
         this.kafkaItem = kafkaItem;
         this.persistentItem = persistentItem;
         this.state = ItemState.determineFor(kafkaItem, persistentItem);
      }

      /**
       * If there is no entry in the AnalyserItem table, the state is NEW or MONITOR.
       */
      ItemWithState(MasterItem kafkaItem) {
         this(kafkaItem, kafkaItem.isMonitorItem() ? ItemState.MONITOR : ItemState.NEW);
      }

      /**
       * If there is no GTIN, the state is ERROR.
       */
      ItemWithState(MasterItem kafkaItem, ItemState state) {
         this.kafkaItem = kafkaItem;
         var gtin = kafkaItem.getGtin();
         persistentItem = new AnalyserItem(kafkaItem.getMasterKey(), gtin);
         this.state = state;
      }

}