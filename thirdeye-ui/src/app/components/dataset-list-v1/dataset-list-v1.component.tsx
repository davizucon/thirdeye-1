import { Button, Grid, Link } from "@material-ui/core";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import {
    getDatasetsUpdatePath,
    getDatasetsViewPath,
} from "../../utils/routes/routes.util";
import { DatasetListV1Props } from "./dataset-list-v1.interfaces";

export const DatasetListV1: FunctionComponent<DatasetListV1Props> = (
    props: DatasetListV1Props
) => {
    const { t } = useTranslation();
    const [
        selectedDataset,
        setSelectedDataset,
    ] = useState<DataGridSelectionModelV1>();
    const history = useHistory();

    const handleDatasetDelete = (): void => {
        if (!selectedDataset) {
            return;
        }

        const selectedSubScriptionGroupId = selectedDataset
            .rowKeyValues[0] as number;
        const uiDataset = getUiDataset(selectedSubScriptionGroupId);
        if (!uiDataset) {
            return;
        }

        props.onDelete && props.onDelete(uiDataset);
    };

    const getUiDataset = (id: number): UiDataset | null => {
        if (!props.datasets) {
            return null;
        }

        return props.datasets.find((dataset) => dataset.id === id) || null;
    };

    const handleDatasetEdit = (): void => {
        if (!selectedDataset) {
            return;
        }
        const selectedSubScriptionGroupId = selectedDataset
            .rowKeyValues[0] as number;

        history.push(getDatasetsUpdatePath(selectedSubScriptionGroupId));
    };

    const isActionButtonDisable = !(
        selectedDataset && selectedDataset.rowKeyValues.length === 1
    );

    const handleDatasetViewDetailsById = (id: number): void => {
        history.push(getDatasetsViewPath(id));
    };

    const renderLink = ({ rowData }: { rowData: UiDataset }): ReactElement => {
        return (
            <Link onClick={() => handleDatasetViewDetailsById(rowData.id)}>
                {rowData.name}
            </Link>
        );
    };

    const datasetColumns = [
        {
            key: "name",
            dataKey: "name",
            title: t("label.name"),
            width: 0,
            flexGrow: 1,
            sortable: true,
            cellRenderer: renderLink,
        },
        {
            key: "datasourceName",
            dataKey: "datasourceName",
            title: t("label.datasource"),
            width: 0,
            sortable: true,
            flexGrow: 1,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1
                    disableBorder
                    columns={datasetColumns}
                    data={props.datasets}
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    selection={selectedDataset}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasetEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            <Grid>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasetDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedDataset}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
