function ingredientHtml(ingredient) {
    return '<li class="ingredient-row">' + ingredient.name + ' <button type="button" onclick="deleteIngredient(' + ingredient.hamburgerId + ', ' + ingredient.id + ')">Remove</button></li>';
}

function refresh() {
    $.get('/hamburgers').done(function (hamburgers) {
        var burgerRequests = (hamburgers || []).map(function (hamburger) {
            return $.get('/hamburgers/' + hamburger.id + '/ingredients').then(function (ingredients) {
                return {
                    hamburger: hamburger,
                    ingredients: ingredients || []
                };
            });
        });

        Promise.all(burgerRequests).then(function (dataList) {
            var list = '';

            if (dataList.length > 0) {
                dataList.forEach(function (entry) {
                    var ingredients = (entry.ingredients || []).map(function (ingredient) {
                        ingredient.hamburgerId = entry.hamburger.id;
                        return ingredient;
                    });

                    var ingredientContent = ingredients.length > 0
                        ? '<ul>' + ingredients.map(ingredientHtml).join('') + '</ul>'
                        : '<span>No ingredients</span>';

                    list += '<tr>'
                        + '<td>' + entry.hamburger.id + '</td>'
                        + '<td>' + entry.hamburger.name + '</td>'
                        + '<td>' + ingredientContent + '</td>'
                        + '<td>'
                        + '  <input id="ingredient-name-' + entry.hamburger.id + '" type="text" placeholder="Ingredient name">'
                        + '  <button type="button" onclick="addIngredient(' + entry.hamburger.id + ')">Add ingredient</button>'
                        + '</td>'
                        + '<td><button type="button" onclick="deleteHamburger(' + entry.hamburger.id + ')">Delete recipe</button></td>'
                        + '</tr>';
                });

                list = '<table><thead><tr><th>Id</th><th>Recipe</th><th>Ingredients</th><th>Add ingredient</th><th></th></tr></thead><tbody>' + list + '</tbody></table>';
            } else {
                list = 'No recipes in database';
            }

            $('#all-hamburgers').html(list);
        });
    });
}

function deleteHamburger(id) {
    $.ajax('/hamburgers/' + id, { method: 'DELETE' }).then(refresh);
}

function addIngredient(hamburgerId) {
    var ingredientName = $('#ingredient-name-' + hamburgerId).val();
    if (!ingredientName || !ingredientName.trim()) {
        return;
    }

    $.ajax({
        url: '/hamburgers/' + hamburgerId + '/ingredients',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ name: ingredientName.trim() })
    }).then(function () {
        $('#ingredient-name-' + hamburgerId).val('');
        refresh();
    });
}

function deleteIngredient(hamburgerId, ingredientId) {
    $.ajax('/hamburgers/' + hamburgerId + '/ingredients/' + ingredientId, { method: 'DELETE' }).then(refresh);
}

$(document).ready(function () {
    $('#create-hamburger-button').click(function () {
        var hamburgerName = $('#hamburger-name').val();
        if (!hamburgerName || !hamburgerName.trim()) {
            return;
        }

        $.post({
            url: '/hamburgers',
            contentType: 'application/json',
            data: JSON.stringify({ name: hamburgerName.trim() })
        }).then(function () {
            $('#hamburger-name').val('');
            refresh();
        });
    });

    refresh();
});
