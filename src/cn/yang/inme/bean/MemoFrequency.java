package cn.yang.inme.bean;

/**
 * Created by Administrator on 14-7-4.
 */
public enum MemoFrequency {
    ONCE {
        @Override
        public String getLabel() {
            return "一次";
        }
    }, EVERYDAY {
        @Override
        public String getLabel() {
            return "每天";
        }
    }, PERWEEK {
        @Override
        public String getLabel() {
            return "每周";
        }
    }, PERMONTH {
        @Override
        public String getLabel() {
            return "每月";
        }
    }, PERYEAR {
        @Override
        public String getLabel() {
            return "每年";
        }
    };

    public static MemoFrequency getFrequency(String label) {
        if (ONCE.getLabel().equals(label)) {
            return ONCE;
        } else if (EVERYDAY.getLabel().equals(label)) {
            return EVERYDAY;
        } else if (PERWEEK.getLabel().equals(label)) {
            return PERWEEK;
        } else if (PERMONTH.getLabel().equals(label)) {
            return PERMONTH;
        } else if (PERYEAR.getLabel().equals(label)) {
            return PERYEAR;
        } else {
            return null;
        }
    }

    public abstract String getLabel();
}
