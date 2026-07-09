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
        var html = "<table border = '1' width = '90%' align = 'center'>";
        html += '<tr>';
        html += '<td>UserID</td>';
        html += '<td>First Name</td>';
        html += '<td>Last Name</td>';
        html += '<td>SSN</td>';
        html += '<td>Salary</td>';

        for (var i = 0; i < result.length; i++) {
            html += '<tr id = "' + encodeURIComponent(result[i].UserID) + '">';
            html += '<td>' + escapeHtml(result[i].UserID) + '</td>';
            html += '<td>' + escapeHtml(result[i].FirstName) + '</td>';
            html += '<td>' + escapeHtml(result[i].LastName) + '</td>';
            html += '<td>' + escapeHtml(result[i].SSN) + '</td>';
            html += '<td>' + escapeHtml(result[i].Salary) + '</td>';
            html += '</tr>';
        }
        html += '</table>';

        var newdiv = document.createElement("div");
        newdiv.innerHTML = html;
        var container = document.getElementById("hiddenEmployeeRecords");
        container.appendChild(newdiv);
    });
}

function escapeHtml(string) {
    var entityMap = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
        '/': '&#x2F;',
        '`': '&#x60;',
        '=': '&#x3D;'
    };
    return String(string).replace(/[&<>"]/g, function (s) {
        return entityMap[s];
    });
}