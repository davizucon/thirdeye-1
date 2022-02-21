import React, { FunctionComponent, lazy, Suspense } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute, getAnomaliesAllPath } from "../../utils/routes/routes.util";

const AnomaliesAllPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-all-page" */ "../../pages/anomalies-all-page/anomalies-all-page.component"
    ).then((module) => ({ default: module.AnomaliesAllPage }))
);

const AnomaliesViewPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-view-page" */ "../../pages/anomalies-view-page/anomalies-view-page.component"
    ).then((module) => ({ default: module.AnomaliesViewPage }))
);

const AnomaliesViewIndexPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-view-page" */ "../../pages/anomalies-view-index-page/anomalies-view-index-page.component"
    ).then((module) => ({ default: module.AnomaliesViewIndexPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const AnomaliesRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Anomalies path */}
                <Route exact path={AppRoute.ANOMALIES}>
                    {/* Redirect to anomalies all path */}
                    <Redirect to={getAnomaliesAllPath()} />
                </Route>

                {/* Anomalies all path */}
                <Route
                    exact
                    component={AnomaliesAllPage}
                    path={AppRoute.ANOMALIES_ALL}
                />

                {/* Anomalies view index path to change time range*/}
                <Route
                    exact
                    component={AnomaliesViewIndexPage}
                    path={AppRoute.ANOMALIES_VIEW_INDEX}
                />

                {/* Anomalies view path */}
                <Route
                    exact
                    component={AnomaliesViewPage}
                    path={AppRoute.ANOMALIES_VIEW}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
