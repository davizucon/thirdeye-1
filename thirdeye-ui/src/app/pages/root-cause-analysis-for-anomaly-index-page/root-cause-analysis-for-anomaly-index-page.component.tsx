import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../platform/rest/actions.interfaces";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { isValidNumberId } from "../../utils/params/params.util";
import { getRootCauseAnalysisForAnomalyInvestigatePath } from "../../utils/routes/routes.util";
import { WEEK_IN_MILLISECONDS } from "../../utils/time/time.util";
import { AnomaliesViewPageParams } from "../anomalies-view-page/anomalies-view-page.interfaces";

export const RootCauseAnalysisForAnomalyIndexPage: FunctionComponent = () => {
    const {
        anomaly,
        getAnomaly,
        status: anomalyRequestStatus,
        errorMessages,
    } = useGetAnomaly();
    const params = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        !!params.id &&
            isValidNumberId(params.id) &&
            getAnomaly(toNumber(params.id));
    }, []);

    useEffect(() => {
        if (anomaly) {
            const timeRangeQuery = new URLSearchParams([
                [TimeRangeQueryStringKey.TIME_RANGE, TimeRange.CUSTOM],
                [
                    TimeRangeQueryStringKey.START_TIME,
                    (anomaly.startTime - WEEK_IN_MILLISECONDS * 2).toString(),
                ],
                [
                    TimeRangeQueryStringKey.END_TIME,
                    (anomaly.endTime + WEEK_IN_MILLISECONDS * 2).toString(),
                ],
            ]);
            navigate(
                `${getRootCauseAnalysisForAnomalyInvestigatePath(
                    toNumber(params.id)
                )}?${timeRangeQuery.toString()}`,
                {
                    replace: true,
                }
            );
        }
    }, [anomaly]);

    useEffect(() => {
        if (anomalyRequestStatus === ActionStatus.Error) {
            isEmpty(errorMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.anomaly"),
                      })
                  )
                : errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  );
        }
    }, [anomalyRequestStatus, errorMessages]);

    return <AppLoadingIndicatorV1 />;
};
