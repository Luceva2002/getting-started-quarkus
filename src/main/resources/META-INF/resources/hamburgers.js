function refresh() {
    $.get('/hamburgers', function (hamburgers) {
        var list = '';
        (hamburgers || []).forEach(function (hamburger) {
            list = list
                + '<tr>'
                + '<td>' + hamburger.id + '</td>'
                + '<td>' + hamburger.name + '</td>'
                + '<td><a href="#" onclick="deletehamburger(' + hamburger.id + ')">Delete</a></td>'
                + '</tr>'
        });
        if (list.length > 0) {
            list = ''
                + '<table><thead><th>Id</th><th>Name</th><th></th></thead>'
                + list
                + '</table>';
        } else {
            list = "No hamburgers in database"
        }
        $('#all-hamburgers').html(list);
    });
}

function deletehamburger(id) {
    $.ajax('/hamburgers/' + id, {method: 'DELETE'}).then(refresh);
}


$(document).ready(function () {

    $('#create-hamburger-button').click(function () {
        var hamburgerName = $('#hamburger-name').val();
        $.post({
            url: '/hamburgers',
            contentType: 'application/json',
            data: JSON.stringify({name: hamburgerName})
        }).then(refresh);
    });

    refresh();
});
