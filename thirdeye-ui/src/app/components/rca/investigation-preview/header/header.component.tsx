/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Box,
    FormControlLabel,
    Grid,
    Radio,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ChartType } from "../investigation-preview.interfaces";
import { HeaderProps } from "./header.interfaces";

export const Header: FunctionComponent<HeaderProps> = ({
    selectedChartType,
    onOptionClick,
    title,
}) => {
    const { t } = useTranslation();

    return (
        <Grid container alignItems="center" justifyContent="space-between">
            <Grid item>
                <Typography variant="h4">{title}</Typography>
            </Grid>
            <Grid item>
                <Box display="inline-block" marginRight={2}>
                    {t("label.chart-view")}
                </Box>
                <FormControlLabel
                    control={
                        <Radio
                            checked={selectedChartType === ChartType.ONE}
                            color="primary"
                        />
                    }
                    label={t("label.consolidated")}
                    value={ChartType.ONE}
                    onClick={() => onOptionClick(ChartType.ONE)}
                />
                <FormControlLabel
                    control={
                        <Radio
                            checked={selectedChartType === ChartType.MULTI}
                            color="primary"
                        />
                    }
                    label={t("label.split")}
                    value={ChartType.MULTI}
                    onClick={() => onOptionClick(ChartType.MULTI)}
                />
            </Grid>
        </Grid>
    );
};
