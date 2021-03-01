import { cloneDeep } from "lodash";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    UiSubscriptionGroup,
    UiSubscriptionGroupAlert,
} from "../../rest/dto/ui-subscription-group.interfaces";
import {
    createEmptySubscriptionGroup,
    createEmptyUiSubscriptionGroup,
    createEmptyUiSubscriptionGroupAlert,
    filterSubscriptionGroups,
    getUiSubscriptionGroup,
    getUiSubscriptionGroupAlert,
    getUiSubscriptionGroupAlertId,
    getUiSubscriptionGroupAlertName,
    getUiSubscriptionGroupAlerts,
    getUiSubscriptionGroups,
} from "./subscription-groups.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("../number/number.util", () => ({
    formatNumber: jest.fn().mockImplementation((num) => num.toString()),
}));

describe("Subscription Groups Util", () => {
    test("createEmptySubscriptionGroup should create appropriate subscription group", () => {
        expect(createEmptySubscriptionGroup()).toEqual(
            mockEmptySubscriptionGroup
        );
    });

    test("createEmptyUiSubscriptionGroup should create appropriate UI subscription group", () => {
        expect(createEmptyUiSubscriptionGroup()).toEqual(
            mockEmptyUiSubscriptionGroup
        );
    });

    test("createEmptyUiSubscriptionGroupAlert should create appropriate UI subscription group alert", () => {
        expect(createEmptyUiSubscriptionGroupAlert()).toEqual(
            mockEmptyUiSubscriptionGroupAlert
        );
    });

    test("getUiSubscriptionGroup should return empty UI subscription group for invalid subscription group", () => {
        expect(
            getUiSubscriptionGroup(
                (null as unknown) as SubscriptionGroup,
                mockAlerts
            )
        ).toEqual(mockEmptyUiSubscriptionGroup);
    });

    test("getUiSubscriptionGroup should return appropriate UI subscription group for subscription group and invalid alerts", () => {
        const expectedUiSubscriptionGroup = cloneDeep(mockUiSubscriptionGroup1);
        expectedUiSubscriptionGroup.alerts = [];
        expectedUiSubscriptionGroup.alertCount = "0";

        expect(
            getUiSubscriptionGroup(
                mockSubscriptionGroup1,
                (null as unknown) as Alert[]
            )
        ).toEqual(expectedUiSubscriptionGroup);
    });

    test("getUiSubscriptionGroup should return appropriate UI subscription group for subscription group and empty alerts", () => {
        const expectedUiSubscriptionGroup = cloneDeep(mockUiSubscriptionGroup1);
        expectedUiSubscriptionGroup.alerts = [];
        expectedUiSubscriptionGroup.alertCount = "0";

        expect(getUiSubscriptionGroup(mockSubscriptionGroup1, [])).toEqual(
            expectedUiSubscriptionGroup
        );
    });

    test("getUiSubscriptionGroup should return appropriate UI subscription group for subscription group and alerts", () => {
        expect(
            getUiSubscriptionGroup(mockSubscriptionGroup1, mockAlerts)
        ).toEqual(mockUiSubscriptionGroup1);
    });

    test("getUiSubscriptionGroups should return empty array for invalid subscription groups", () => {
        expect(
            getUiSubscriptionGroups(
                (null as unknown) as SubscriptionGroup[],
                mockAlerts
            )
        ).toEqual([]);
    });

    test("getUiSubscriptionGroups should return empty array for empty subscription groups", () => {
        expect(getUiSubscriptionGroups([], mockAlerts)).toEqual([]);
    });

    test("getUiSubscriptionGroups should return appropriate UI subscription groups for subscription groups and invalid alerts", () => {
        const expectedUiSubscriptionGroup1 = cloneDeep(
            mockUiSubscriptionGroup1
        );
        expectedUiSubscriptionGroup1.alerts = [];
        expectedUiSubscriptionGroup1.alertCount = "0";
        const expectedUiSubscriptionGroup2 = cloneDeep(
            mockUiSubscriptionGroup2
        );
        expectedUiSubscriptionGroup2.alerts = [];
        expectedUiSubscriptionGroup2.alertCount = "0";
        const expectedUiSubscriptionGroup3 = cloneDeep(
            mockUiSubscriptionGroup3
        );
        expectedUiSubscriptionGroup3.alerts = [];
        expectedUiSubscriptionGroup3.alertCount = "0";

        expect(
            getUiSubscriptionGroups(
                mockSubscriptionGroups,
                (null as unknown) as Alert[]
            )
        ).toEqual([
            expectedUiSubscriptionGroup1,
            expectedUiSubscriptionGroup2,
            expectedUiSubscriptionGroup3,
        ]);
    });

    test("getUiSubscriptionGroups should return appropriate UI subscription groups for subscription groups and empty alerts", () => {
        const expectedUiSubscriptionGroup1 = cloneDeep(
            mockUiSubscriptionGroup1
        );
        expectedUiSubscriptionGroup1.alerts = [];
        expectedUiSubscriptionGroup1.alertCount = "0";
        const expectedUiSubscriptionGroup2 = cloneDeep(
            mockUiSubscriptionGroup2
        );
        expectedUiSubscriptionGroup2.alerts = [];
        expectedUiSubscriptionGroup2.alertCount = "0";
        const expectedUiSubscriptionGroup3 = cloneDeep(
            mockUiSubscriptionGroup3
        );
        expectedUiSubscriptionGroup3.alerts = [];
        expectedUiSubscriptionGroup3.alertCount = "0";

        expect(getUiSubscriptionGroups(mockSubscriptionGroups, [])).toEqual([
            expectedUiSubscriptionGroup1,
            expectedUiSubscriptionGroup2,
            expectedUiSubscriptionGroup3,
        ]);
    });

    test("getUiSubscriptionGroups should return appropriate UI subscription groups for subscription groups and alerts", () => {
        expect(
            getUiSubscriptionGroups(mockSubscriptionGroups, mockAlerts)
        ).toEqual(mockUiSubscriptionGroups);
    });

    test("getUiSubscriptionGroupAlert should return empty UI subscription group alert for invalid alert", () => {
        expect(getUiSubscriptionGroupAlert((null as unknown) as Alert)).toEqual(
            mockEmptyUiSubscriptionGroupAlert
        );
    });

    test("getUiSubscriptionGroupAlert should return appropriate UI subscription group alert for alert", () => {
        expect(getUiSubscriptionGroupAlert(mockAlert1)).toEqual(
            mockUiSubscriptionGroupAlert1
        );
    });

    test("getUiSubscriptionGroupAlerts should return empty array for invalid alerts", () => {
        expect(
            getUiSubscriptionGroupAlerts((null as unknown) as Alert[])
        ).toEqual([]);
    });

    test("getUiSubscriptionGroupAlerts should return empty array for empty alerts", () => {
        expect(getUiSubscriptionGroupAlerts([])).toEqual([]);
    });

    test("getUiSubscriptionGroupAlerts should return appropriate UI subscription group alerts for alerts", () => {
        expect(getUiSubscriptionGroupAlerts(mockAlerts)).toEqual(
            mockUiSubscriptionGroupAlerts
        );
    });

    test("getUiSubscriptionGroupAlertId should return -1 for invalid UI subscription group alert", () => {
        expect(
            getUiSubscriptionGroupAlertId(
                (null as unknown) as UiSubscriptionGroupAlert
            )
        ).toEqual(-1);
    });

    test("getUiSubscriptionGroupAlertId should return approopriate id for UI subscription group alert", () => {
        expect(
            getUiSubscriptionGroupAlertId(mockUiSubscriptionGroupAlert1)
        ).toEqual(2);
    });

    test("getUiSubscriptionGroupAlertName should return empty string for invalid UI subscription group alert", () => {
        expect(
            getUiSubscriptionGroupAlertName(
                (null as unknown) as UiSubscriptionGroupAlert
            )
        ).toEqual("");
    });

    test("getUiSubscriptionGroupAlertName should return approopriate name for UI subscription group alert", () => {
        expect(
            getUiSubscriptionGroupAlertName(mockUiSubscriptionGroupAlert1)
        ).toEqual("testNameAlert2");
    });

    test("filterSubscriptionGroups should return empty array for invalid UI subscription groups", () => {
        expect(
            filterSubscriptionGroups(
                (null as unknown) as UiSubscriptionGroup[],
                mockSearchWords
            )
        ).toEqual([]);
    });

    test("filterSubscriptionGroups should return empty array for empty UI subscription groups", () => {
        expect(filterSubscriptionGroups([], mockSearchWords)).toEqual([]);
    });

    test("filterSubscriptionGroups should return appropriate UI subscription groups for UI subscription groups and invalid search words", () => {
        expect(
            filterSubscriptionGroups(
                mockUiSubscriptionGroups,
                (null as unknown) as string[]
            )
        ).toEqual(mockUiSubscriptionGroups);
    });

    test("filterSubscriptionGroups should return appropriate UI subscription groups for UI subscription groups and empty search words", () => {
        expect(filterSubscriptionGroups(mockUiSubscriptionGroups, [])).toEqual(
            mockUiSubscriptionGroups
        );
    });

    test("filterSubscriptionGroups should return appropriate UI subscription groups for UI subscription groups and search words", () => {
        expect(
            filterSubscriptionGroups(mockUiSubscriptionGroups, mockSearchWords)
        ).toEqual([mockUiSubscriptionGroup1, mockUiSubscriptionGroup3]);
    });
});

const mockEmptySubscriptionGroup = {
    name: "",
    alerts: [],
    notificationSchemes: {
        email: {
            to: [],
        },
    },
};

const mockEmptyUiSubscriptionGroup = {
    id: -1,
    name: "label.no-data-marker",
    alerts: [],
    alertCount: "0",
    emails: [],
    emailCount: "0",
    subscriptionGroup: null,
};

const mockEmptyUiSubscriptionGroupAlert = {
    id: -1,
    name: "label.no-data-marker",
};

const mockSubscriptionGroup1 = {
    id: 1,
    name: "testNameSubscriptionGroup1",
    alerts: [
        {
            id: 2,
        },
        {
            id: 3,
        },
        {
            id: 4,
        },
    ],
    notificationSchemes: {
        email: {
            to: [
                "testEmail1SubscriptionGroup1",
                "testEmail2SubscriptionGroup1",
            ],
        },
    },
} as SubscriptionGroup;

const mockSubscriptionGroup2 = {
    id: 5,
    alerts: [] as Alert[],
    notificationSchemes: {},
} as SubscriptionGroup;

const mockSubscriptionGroup3 = {
    id: 6,
    name: "testNameSubscriptionGroup6",
} as SubscriptionGroup;

const mockSubscriptionGroups = [
    mockSubscriptionGroup1,
    mockSubscriptionGroup2,
    mockSubscriptionGroup3,
];

const mockAlert1 = {
    id: 2,
    name: "testNameAlert2",
} as Alert;

const mockAlert2 = {
    id: 3,
} as Alert;

const mockAlert3 = {
    id: 6,
    name: "testNameAlert6",
} as Alert;

const mockAlerts = [mockAlert1, mockAlert2, mockAlert3];

const mockUiSubscriptionGroupAlert1 = {
    id: 2,
    name: "testNameAlert2",
};

const mockUiSubscriptionGroupAlert2 = {
    id: 3,
    name: "label.no-data-marker",
};

const mockUiSubscriptionGroupAlert3 = {
    id: 6,
    name: "testNameAlert6",
};

const mockUiSubscriptionGroupAlerts = [
    mockUiSubscriptionGroupAlert1,
    mockUiSubscriptionGroupAlert2,
    mockUiSubscriptionGroupAlert3,
];

const mockUiSubscriptionGroup1 = {
    id: 1,
    name: "testNameSubscriptionGroup1",
    alerts: [
        {
            id: 2,
            name: "testNameAlert2",
        },
        {
            id: 3,
            name: "label.no-data-marker",
        },
    ],
    alertCount: "2",
    emails: ["testEmail1SubscriptionGroup1", "testEmail2SubscriptionGroup1"],
    emailCount: "2",
    subscriptionGroup: mockSubscriptionGroup1,
};

const mockUiSubscriptionGroup2 = {
    id: 5,
    name: "label.no-data-marker",
    alerts: [],
    alertCount: "0",
    emails: [],
    emailCount: "0",
    subscriptionGroup: mockSubscriptionGroup2,
};

const mockUiSubscriptionGroup3 = {
    id: 6,
    name: "testNameSubscriptionGroup6",
    alerts: [],
    alertCount: "0",
    emails: [],
    emailCount: "0",
    subscriptionGroup: mockSubscriptionGroup3,
};

const mockUiSubscriptionGroups = [
    mockUiSubscriptionGroup1,
    mockUiSubscriptionGroup2,
    mockUiSubscriptionGroup3,
];

const mockSearchWords = ["testNameAlert2", "testNameSubscriptionGroup6"];
