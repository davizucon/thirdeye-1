import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";

export interface AlertEvaluationTimeSeriesCardProps {
    maximized?: boolean;
    alertEvaluationTimeSeriesHeight: number;
    alertEvaluationTimeSeriesMaximizedHeight?: number;
    title?: string;
    maximizedTitle?: string;
    alertEvaluation: AlertEvaluation | null;
    error?: boolean;
    helperText?: string;
    hideRefreshButton?: boolean;
    onRefresh?: () => void;
}
