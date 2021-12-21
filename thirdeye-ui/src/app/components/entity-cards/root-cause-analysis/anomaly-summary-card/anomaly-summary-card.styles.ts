import { makeStyles } from "@material-ui/core";

export const useAnomalySummaryCardStyles = makeStyles(() => ({
    label: {
        color: "#6A6C75",
        marginTop: "6px",
    },
    valueText: {
        fontSize: "24px",
    },
    deviationValue: {
        color: "#EE0202",
    },
}));
