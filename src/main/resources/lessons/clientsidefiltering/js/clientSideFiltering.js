var dataFetched = false;

function selectUser() {

    var newEmployeeID = $("#UserSelect").val();
    var employeeRecordElement = document.getElementById(newEmployeeID);
    if (employeeRecordElement) {
        var sanitizedContent = document.createTextNode(employeeRecordElement.innerHTML);
        var container = document.getElementById("employeeRecord");
        container.textContent = ""; // Clear existing content
        container.appendChild(sanitizedContent);
    }
}

function fetchUserData() {
    if (!dataFetched) {
        dataFetched = true;
        ajaxFunction(document.getElementById("userID").value);
    }
}

function ajaxFunction(userId) {
    $.get("clientSideFiltering/salaries?userId=" + encodeURIComponent(userId), function (result, status) {
        var html = document.createElement("table");
        html.setAttribute("border", "1");
        html.setAttribute("width", "90%");
        html.setAttribute("align", "center");

        var headerRow = document.createElement("tr");
        ["UserID", "First Name", "Last Name", "SSN", "Salary"].forEach(function (header) {
            var th = document.createElement("td");
            th.textContent = header;
            headerRow.appendChild(th);
        });
        html.appendChild(headerRow);

        for (var i = 0; i < result.length; i++) {
            var row = document.createElement("tr");
            row.setAttribute("id", result[i].UserID);

            [result[i].UserID, result[i].FirstName, result[i].LastName, result[i].SSN, result[i].Salary].forEach(function (cellData) {
                var td = document.createElement("td");
                td.textContent = cellData;
                row.appendChild(td);
            });

            html.appendChild(row);
        }

        var newdiv = document.createElement("div");
        newdiv.appendChild(html);
        var container = document.getElementById("hiddenEmployeeRecords");
        container.appendChild(newdiv);
    });
}