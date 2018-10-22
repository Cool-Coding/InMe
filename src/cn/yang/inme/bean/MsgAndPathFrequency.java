package cn.yang.inme.bean;

/**
 * Created by Administrator on 14-7-18.
 */
public enum MsgAndPathFrequency {
    TODAY {
        @Override
        public String label() {
            return "今天";
        }
    }, WEEK {
        @Override
        public String label() {
            return "本周";
        }
    }, MONTH {
        @Override
        public String label() {
            return "本月";
        }
    }, YEAR {
        @Override
        public String label() {
            return "本年";
        }
    }, ALL {
        @Override
        public String label() {
            return "全部";
        }
    };

    /**
     * 根据标签反转成频率对象
     *
     * @param label 标签
     * @return
     */
    public static MsgAndPathFrequency getFrequencyFromLabel(String label) {
        if (TODAY.label().equals(label)) {
            return TODAY;
        } else if (WEEK.label().equals(label)) {
            return WEEK;
        } else if (MONTH.label().equals(label)) {
            return MONTH;
        } else if (YEAR.label().equals(label)) {
            return YEAR;
        } else if (ALL.label().equals(label)) {
            return ALL;
        } else return null;
    }

    ;

    public abstract String label();
}
