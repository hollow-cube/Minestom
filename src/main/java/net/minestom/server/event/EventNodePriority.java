package net.minestom.server.event;

public class EventNodePriority {
    public enum Absolute {
        FIRST,
        LAST,
        NONE;

        public Absolute reverse() {
            return switch (this) {
                case FIRST -> LAST;
                case LAST -> FIRST;
                case NONE -> NONE;
            };
        }
    }


    public enum Relative {
        BEFORE,
        AFTER,
        NONE;
        public Relative reverse() {
            return switch (this) {
                case BEFORE -> AFTER;
                case AFTER -> BEFORE;
                case NONE -> NONE;
            };
        }
    }
}
