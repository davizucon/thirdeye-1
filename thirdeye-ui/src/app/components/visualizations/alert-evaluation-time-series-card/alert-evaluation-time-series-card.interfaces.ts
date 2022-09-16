/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";

export interface AlertEvaluationTimeSeriesCardProps {
    alertEvaluationTimeSeriesHeight: number;
    alertEvaluation: AlertEvaluation | null;
    onRefresh?: (start?: number, end?: number) => void;
    isLoading?: boolean;
    header?: React.ReactElement;
    anomalies: Anomaly[];
    disableNavigation?: boolean;
}

export interface ViewAnomalyHeaderProps {
    anomaly: Anomaly | null;
    onRefresh?: (start?: number, end?: number) => void;
}

export interface CreateAlertHeaderProps {
    onRefresh?: (start?: number, end?: number) => void;
    title: string;
}
